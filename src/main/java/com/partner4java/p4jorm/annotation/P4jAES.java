package com.partner4java.p4jorm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * aes加密
 * 
 * @author 王昌龙
 * 
 */
@Target({ java.lang.annotation.ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface P4jAES {

	/**
	 * 加密密钥
	 * 
	 * @return
	 */
	public abstract String value() default "longge";
}
