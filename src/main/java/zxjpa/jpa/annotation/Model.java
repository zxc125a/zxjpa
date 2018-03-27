package zxjpa.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Primary
public @interface Model {
	/**
	 * 使用的数据源
	 */
	String dsname() default "default";
	/**
	 * 是否开启分片
	 */
	boolean sharding() default false;
}
