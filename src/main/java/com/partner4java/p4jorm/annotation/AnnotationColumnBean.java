package com.partner4java.p4jorm.annotation;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * 用户封装存放Entity每个字段的相关映射数据
 * 
 * @author partner4java
 * 
 */
public class AnnotationColumnBean implements Serializable {
	private static final long serialVersionUID = 7115326774324941910L;

	/** 对应字段的反射类型 */
	private Field field;

	/** 对应字段的数据名称 */
	private String columnName;

	/** 是否为主键id */
	private boolean isId = false;

	/**
	 * 字段是否自增<br/>
	 * 默认值为true，标识id由数据库生成，如果手工赋值为false，标识由框架生成id
	 */
	private boolean autoIncrement = true;

	/** 是否加密 */
	private boolean isAes = false;

	/** 使用的aes密钥 */
	private String aes;

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isId() {
		return isId;
	}

	public void setIsId(boolean isId) {
		this.isId = isId;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public void setId(boolean isId) {
		this.isId = isId;
	}

	public boolean isAes() {
		return isAes;
	}

	public void setAes(boolean isAes) {
		this.isAes = isAes;
	}

	public String getAes() {
		return aes;
	}

	public void setAes(String aes) {
		this.aes = aes;
	}

	@Override
	public String toString() {
		return "AnnotationColumnBean [field=" + field + ", columnName=" + columnName + ", isId=" + isId
				+ ", autoIncrement=" + autoIncrement + ", isAes=" + isAes + ", aes=" + aes + "]";
	}

}
