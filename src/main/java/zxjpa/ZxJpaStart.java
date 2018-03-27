package zxjpa;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import zxjpa.cache.redis.RedisCacheManager;
import zxjpa.config.ZxJpaConfig;
import zxjpa.jpa.datasource.DataSourceManager;
import zxjpa.util.ScanningClass;

@Component
public class ZxJpaStart {
	public void start(ApplicationContext applicationContext) {
		//类扫描，加载需要的模型
		ScanningClass.init();
		//加载xml配置
		ZxJpaConfig.loadZxJpaConfig();
		//redis加载
		RedisCacheManager.init();
		//数据库加载
		DataSourceManager.init();
		//定期检测读库熔断
		// TODO: 
		//定期检测泄漏事务存储
		// TODO: 
		//定期检测数据源配置，热更新
		// TODO:
	}
}
