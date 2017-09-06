package com.partner4java.p4jorm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加入类上，表明本类的字段赋值会用于简易ORM支持(必须指定本注解)。<br/>
 * 提供了统一配置：如表名、是否添加无赋值的字段等<br/>
 * 二个属性tableName、addEmpty，具体查看各属性单独说明。<br/>
 * <b>注意：</b><br/>
 * 1\标注此注解的类必须包含无参构造器。<br>
 * 2\本注解会默认添加类里所有字段为数据库对应字段，如果想排除可添加注解P4jTransient（serialVersionUID会自动排除）<br/>
 * 
 * @author partner4java
 * 
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface P4jEntity {
	/**
	 * 表名，用于Jdbc（默认为空字符串）
	 * 
	 * @return
	 */
	public abstract String tableName() default "";

	/**
	 * 是否添加无赋值的字段(默认为false)<br/>
	 * 包括插入和修改等
	 * 
	 * @return
	 */
	public abstract boolean addEmpty() default false;
}
