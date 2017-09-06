package com.partner4java.p4jorm.dao.enums;

/**
 * 排序规则
 * 
 * @author partner4java
 * 
 */
public enum OrderType {
	DESC {
		@Override
		public String getStr() {
			return DESC_STR;
		}
	},ASC {
		@Override
		public String getStr() {
			return ASC_STR;
		}
	};
	private static final String ASC_STR = "asc";
	private static final String DESC_STR = "desc";

	public abstract String getStr();
}
