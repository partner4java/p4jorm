package com.partner4java.p4jorm.utils;

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * 使每个表前面加上"t_"
 * 
 * @author partner4java
 * 
 */
public class PANamingStrategy extends ImprovedNamingStrategy {
	private static final long serialVersionUID = -3210031607960659652L;

	/**
	 * 当没有声明显示的名称时调用
	 */
	@Override
	public String classToTableName(String className) {
		return "t_" + super.classToTableName(className);
	}

	/**
	 * 当声明了显示的名称时调用 Alter the table name given in the mapping document
	 */
	@Override
	public String tableName(String tableName) {
		return super.tableName(tableName);
	}

}