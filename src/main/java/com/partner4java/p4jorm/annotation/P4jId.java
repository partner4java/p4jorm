package com.partner4java.p4jorm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于声明某字段为数据表主键，用于排除在insert、update的字段外。<br/>
 * 一个属性fieldName，具体查看各属性单独说明。<br/>
 * <b>注意：</b>插入的主键字段赋值需数据库自行解决（如MySQL的AUTO_INCREMENT）。
 * 
 * @author partner4java
 * 
 */
@Target({ java.lang.annotation.ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface P4jId {
	/**
	 * 字段名称<br/>
	 * 如果不指定，会默认以被标注字段名称为查询字段名称
	 * 
	 * @return
	 */
	public abstract String columnName() default "";

	/**
	 * 字段是否自增<br/>
	 * 默认值为true，标识id由数据库生成，如果手工赋值为false，标识由框架生成id；<br/>
	 * <B>注意：</B>如果设置为false，字段必须为long类型。
	 * 
	 * @return
	 */
	public abstract boolean autoIncrement() default true;
}
