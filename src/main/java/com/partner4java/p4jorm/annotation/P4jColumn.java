package com.partner4java.p4jorm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于声明某字段为数据库对应字段<br/>
 * 一个属性fieldName，具体查看各属性单独说明。
 * 
 * @author partner4java
 * 
 */
@Target({ java.lang.annotation.ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface P4jColumn {
	/**
	 * 字段名称<br/>
	 * 如果不指定，会默认以被标注字段名称为查询字段名称
	 * 
	 * @return
	 */
	public abstract String columnName() default "";
}
