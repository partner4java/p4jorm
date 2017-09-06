package com.partner4java.p4jorm.dao.enums;

/**
 * 拼写SQL判断条件的枚举类型
 * 
 * @author partner4java
 * 
 */
public enum GeneralQueryType {
	/** 等于 */
	EQ {
		@Override
		public String getStr() {
			return " = ";
		}
	},
	/** 大于 */
	GR {
		@Override
		public String getStr() {
			return " > ";
		}
	},
	/** 小于 */
	LE {
		@Override
		public String getStr() {
			return " < ";
		}
	},
	/** 大约等于 */
	GREQ {
		@Override
		public String getStr() {
			return " >= ";
		}
	},
	/** 小于等于 */
	LEEQ {
		@Override
		public String getStr() {
			return " <= ";
		}
	},
	/** 不等于 */
	NOEQ {
		@Override
		public String getStr() {
			return " <> ";
		}
	},
	/** in查询 */
	IN{

		@Override
		public String getStr() {
			return " in ";
		}
		
	};

	/**
	 * 获取SQL拼写字符串,本身携带了前后空格
	 * 
	 * @return
	 */
	public abstract String getStr();
}
