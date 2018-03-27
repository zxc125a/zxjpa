package zxjpa.cache.transaction;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import zxjpa.aop.ServiceAspect;
import zxjpa.cache.mgr.CacheManager;
import zxjpa.cache.mgr.CacheModelManager;
import zxjpa.cache.model.CacheModel;
import zxjpa.config.ZxJpaConfig;

/**
 * 缓存事务管理
 * @author zx
 *
 */
@Component
public class CacheTransaction {
	private Logger logger = LoggerFactory.getLogger(CacheTransaction.class);  
	//事务：模型组：ID键：值
	public ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Object>>> transactionMap=new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Object>>>();
	@Resource
	CacheManager cmm;
	/**
	 * 预提交缓存
	 * @param transactionId
	 * @param group
	 * @param id
	 * @param value
	 */
	public void put(CacheModel cacheModel,String id,Object value) {
		try {
			String transactionId = Thread.currentThread().getName();
			if(transactionId.startsWith(ServiceAspect.THREADNAMESTARTS)) {
				if(value!=null&&CacheModelManager.checkCacheModel(cacheModel)) {
					getGroupMap(transactionId, cacheModel.getCacheGroup()).put(id, value);
				}
			}else {
				logger.error("cache put失败，未进入事务中。");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 执行删除不在事务颗粒度中
	 * @param group
	 * @param id
	 */
	public void remove(String group,String id) {
		try {
			String transactionId = Thread.currentThread().getName();
			if(transactionId.startsWith(ServiceAspect.THREADNAMESTARTS)) {
				getGroupMap(transactionId, group).remove(id);
				cmm.remove(group, id);
			}else {
				logger.error("cache remove失败，未进入事务中。");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获得事务中的值
	 * @param transactionId
	 * @param group
	 * @param key
	 * @return
	 */
	public Object get(String group,String id) {
		try {
			String transactionId = Thread.currentThread().getName();
			Object object = getGroupMap(transactionId, group).get(id);
			if(object == null) {
				//调用缓存manager尝试获取
				return cmm.get(group, id);
			}else {
				return object;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 事务开始
	 */
	public void begin() {
		String transactionId = Thread.currentThread().getName();
		transactionMap.put(transactionId, new ConcurrentHashMap<String, ConcurrentHashMap<String, Object>>());
	}
	/**
	 * 提交事务
	 */
	public void commit() {
		try {
			String transactionId = Thread.currentThread().getName();
			if(transactionId.startsWith(ServiceAspect.THREADNAMESTARTS)) {
				//调用缓存manager存值
				ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> ctransactionMap = transactionMap.get(transactionId);
				Enumeration<String> gourps = ctransactionMap.keys();
				while(gourps.hasMoreElements()) {
					String group = gourps.nextElement();
					ConcurrentHashMap<String, Object> kvMap = ctransactionMap.get(group);
					Enumeration<String> keys = kvMap.keys();
					while(keys.hasMoreElements()) {
						String id = keys.nextElement();
						Object value = kvMap.get(id);
						cmm.put(CacheModelManager.getCacheModelByCacheGroup(group), id, value);
					}
				}
			}else {
				logger.error("cache commit失败，未进入事务中。");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 回滚事务
	 */
	public void rollback() {
	}
	/**
	 * 清理事务
	 */
	public void clear() {
		try {
			String transactionId = Thread.currentThread().getName();
			transactionMap.remove(transactionId);
			if(ZxJpaConfig.showlog) {
				logger.info("cache transaction clear:"+transactionId+" cacheTransactionSize:"+transactionMap.size());
			}
			cmm.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获取组map
	 * @param transactionId
	 * @param group
	 * @return
	 * @throws Exception
	 */
	private ConcurrentHashMap<String, Object> getGroupMap(String transactionId,String group){
		ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> ctransactionMap = transactionMap.get(transactionId);
		if(ctransactionMap==null) {
			logger.error("缓存事务未创建，请在service层使用缓存相关功能");
		}
		ConcurrentHashMap<String, Object> groupMap = ctransactionMap.get(group);
		if(groupMap==null) {
			groupMap=new ConcurrentHashMap<String, Object>();
			ctransactionMap.put(group, groupMap);
		}
		return groupMap;
	}
}
