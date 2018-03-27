package zxjpa.cache.mgr;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import zxjpa.cache.local.LocalCacheManager;
import zxjpa.cache.model.CacheModel;
import zxjpa.cache.redis.RedisCacheManager;
import zxjpa.cache.transaction.CacheTransaction;
import zxjpa.util.JsonUtil;

/**
 * 缓存管理类
 * @author zx
 *
 */
@Component
public class CacheManager {
	private Logger logger = LoggerFactory.getLogger(CacheTransaction.class);
	@Resource
	RedisCacheManager rcm;
	@Resource
	LocalCacheManager lcm;
	public void put(CacheModel cm,String id,Object value) {
		if(cm==null) {
			logger.error("CacheManager.put失败,CacheModel不能为空");
			return;
		}
		if(!CacheModelManager.checkCacheModel(cm)) {
			logger.error("CacheManager.put失败,无效的CacheGroup,请给["+cm.getCacheGroup()+"]该对象添加Cache注解。");
			return;
		}
		if(cm.isLcCache()) {
			lcm.put(cm.getCacheGroup(), id, value);
		}
		if(cm.isRcCache()) {
			rcm.put(cm, id, value);
		}
	}

	public Object get(String group,String id) {
		CacheModel cm = CacheModelManager.getCacheModelByCacheGroup(group);
		if(cm.isLcCache()&&cm.isRcCache()) {
			Object o = lcm.get(group, id);
			if(o==null) {
				o=rcm.get(group, id);
			}
			return o;
		}else if(cm.isLcCache()) {
			return lcm.get(group, id);
		}else if(cm.isRcCache()) {
			return rcm.get(group, id);
		}
		return null;
	}
	public void remove(String group) {
		CacheModel cm = CacheModelManager.getCacheModelByCacheGroup(group);
		if(cm.isLcCache()) {
			lcm.remove(group);
		}
		if(cm.isRcCache()) {
			rcm.remove(group);
		}
	}
	public void remove(String group,String key) {
		CacheModel cm = CacheModelManager.getCacheModelByCacheGroup(group);
		if(cm.isLcCache()) {
			lcm.remove(group,key);
		}
		if(cm.isRcCache()) {
			rcm.remove(group,key);
		}
	}
	/**
	 * 清理查询缓存
	 * @param cm 缓存模型
	 * @param args sql参数
	 */
	public void removeQueryCache(CacheModel cm,Object... args) {
		if(cm.isQueryCache()) {
			remove(cm.getCacheGroup(),getQueryKey(cm.getSql(), args));
		}
	}
	/**
	 * 获得查询key
	 * @param sql
	 * @param args
	 * @return
	 */
	public static String getQueryKey(String sql,Object... args) {
		return sql+(args==null?"":JsonUtil.obj2Json(args));
	}
	/**
	 * 关闭访问资源
	 */
	public void close() {
		rcm.close();
	}
}
