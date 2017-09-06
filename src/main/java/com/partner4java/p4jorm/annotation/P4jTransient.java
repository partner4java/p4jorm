package com.partner4java.p4jorm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于声明某字段,表明该变量不参与映射<br/>
 * 
 * @author partner4java
 * 
 */
@Target({ java.lang.annotation.ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface P4jTransient {

}
