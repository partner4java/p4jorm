package com.partner4java.p4jorm.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加入类上，表明本类的字段赋值会用于查询拼写(Jdbc必须指定本注解)。<br/>
 * 提供了统一配置：如表名、是否添加无赋值的字段等<br/>
 * 三个属性tableName、addEmpty、returnField，具体查看各属性单独说明。
 * 
 * @author partner4java
 * 
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface P4jQueryForm {
	/**
	 * 表名，用于Jdbc（默认为空字符串）
	 * 
	 * @return
	 */
	@Deprecated
	public abstract String tableName() default "";

	/**
	 * 是否添加无赋值的字段(默认为false)
	 * 
	 * @return
	 */
	public abstract boolean addEmpty() default false;

	/**
	 * 返回字段，默认返回所有(*)<br/>
	 * 如果为hibernate,且没有额外指定本字段,需要进行额外判断赋值<br/>
	 * 
	 * @return
	 */
	public abstract String returnField() default "*";

	/**
	 * 排序规则，如“id desc,name desc”；<br/>
	 * <b>注意：</b>如果已经在其他地方指定了排序归着，本指定无效。
	 * 
	 * @return
	 */
	public abstract String orderBy() default "";
}
