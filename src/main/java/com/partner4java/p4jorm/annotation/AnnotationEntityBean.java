package com.partner4java.p4jorm.annotation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 保存具体实体类的映射方式
 * 
 * @author partner4java
 * 
 */
public class AnnotationEntityBean implements Serializable {
	private static final long serialVersionUID = -2163252636042495951L;
	/** 自己的class类型 */
	private Class clazz;
	/** 类上注解 */
	private P4jEntity entity;
	/** 所以字段数据封装 */
	private List<AnnotationColumnBean> annotationColumnBeans = new LinkedList<AnnotationColumnBean>();

	public Class getClazz() {
		return clazz;
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public P4jEntity getEntity() {
		return entity;
	}

	public void setEntity(P4jEntity entity) {
		this.entity = entity;
	}

	public List<AnnotationColumnBean> getAnnotationColumnBeans() {
		return annotationColumnBeans;
	}

	public void setAnnotationColumnBeans(List<AnnotationColumnBean> annotationColumnBeans) {
		this.annotationColumnBeans = annotationColumnBeans;
	}

	@Override
	public String toString() {
		return "AnnotationEntityBean [entity=" + entity + ", annotationColumnBeans=" + annotationColumnBeans + "]";
	}

}
