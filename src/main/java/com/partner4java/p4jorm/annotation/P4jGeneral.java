package com.partner4java.p4jorm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.partner4java.p4jorm.dao.enums.GeneralQueryType;

/**
 * 用于声明某字段为普通查询，如等值查询<br/>
 * 两个属性fieldName、generalQueryType，具体查看各属性单独说明。
 * 
 * @author partner4java
 * 
 */
@Target({ java.lang.annotation.ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface P4jGeneral {
	/**
	 * 字段名称<br/>
	 * 如果不指定，会默认以被标注字段名称为查询字段名称
	 * 
	 * @return
	 */
	public abstract String columnName() default "";

	/**
	 * 判断方式，枚举类型。默认值为等于
	 * 
	 * @return
	 */
	public abstract GeneralQueryType generalQueryType() default GeneralQueryType.EQ;

	/**
	 * 是否添加无赋值的字段(默认为false)<br/>
	 * 常用于对象类型
	 * 
	 * @return
	 */
	public abstract boolean addEmpty() default false;

	/**
	 * 是否添加值为0的字段(默认为false)<br/>
	 * 常用于基本类型
	 * 
	 * @return
	 */
	public abstract boolean addZero() default false;
}
