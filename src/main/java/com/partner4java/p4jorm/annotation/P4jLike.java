package com.partner4java.p4jorm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于声明某字段为like模糊查询。<br/>
 * 两个属性fieldName、placeHolder，具体查看各属性单独说明。
 * 
 * @author partner4java
 * 
 */
@Target({ java.lang.annotation.ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface P4jLike {
	/**
	 * 字段名称<br/>
	 * 如果不指定，会默认以被标注字段名称为查询字段名称
	 * 
	 * @return
	 */
	public abstract String columnName() default "";

	/**
	 * 占位符声明<br/>
	 * 如，我们的赋值为“partner4java”想以“%partner4java%”方式查询。<br/>
	 * 若fileName属性赋值为“username”，那么本字段赋值应该为“%username%”。<br/>
	 * 默认为前后都加“%”。但是，若不想自动添加，需要给本属性赋值查询字段名称。
	 * 
	 * @return
	 */
	public abstract String placeHolder() default "";

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
