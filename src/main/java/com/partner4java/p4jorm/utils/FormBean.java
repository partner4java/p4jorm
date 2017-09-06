package com.partner4java.p4jorm.utils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.partner4java.p4jorm.annotation.AnnotationFormBean;

/**
 * 远程调用formbean封装对象
 * 
 * @author 王昌龙
 * 
 */
public class FormBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Log logger = LogFactory.getLog(FormBean.class);

	/** 返回字段 */
	private String returnFiled;

	/** 排序规则 */
	private String orderBy;

	/** 查询条件 */
	private String sql;

	/** 查询参数值对应 */
	private Map<String, Object> params = new LinkedHashMap<String, Object>();

	/**
	 * 传入formbean构建相应的查询条件
	 * 
	 * @param formbean
	 * @return
	 */
	public static FormBean build(Object formbean) {
		if (formbean != null) {
			FormBean form = new FormBean();
			AnnotationFormBean annotationBean = AnnotationGetHelper.getAnnotationFormBean(formbean.getClass());
			form.setReturnFiled(annotationBean.getQueryForm().returnField());
			form.setSql(SQLSpellHelper.getJdbcWhereSQL(annotationBean, formbean, form.getParams()));
			form.setOrderBy(annotationBean.getQueryForm().orderBy());
			if (logger.isDebugEnabled()) {
				logger.debug("远程调用formbean封装对象:" + form);
			}
			return form;
		}
		return null;
	}

	public String getReturnFiled() {
		return returnFiled;
	}

	public void setReturnFiled(String returnFiled) {
		this.returnFiled = returnFiled;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	@Override
	public String toString() {
		return "FormBean [returnFiled=" + returnFiled + ", orderBy=" + orderBy + ", sql=" + sql + ", params=" + params + "]";
	}

}
