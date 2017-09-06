package com.partner4java.p4jorm.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.AnnotationUtils;

import com.partner4java.p4jorm.annotation.AnnotationColumnBean;
import com.partner4java.p4jorm.annotation.AnnotationEntityBean;
import com.partner4java.p4jorm.annotation.AnnotationFormBean;
import com.partner4java.p4jorm.annotation.P4jAES;
import com.partner4java.p4jorm.annotation.P4jColumn;
import com.partner4java.p4jorm.annotation.P4jEntity;
import com.partner4java.p4jorm.annotation.P4jGeneral;
import com.partner4java.p4jorm.annotation.P4jId;
import com.partner4java.p4jorm.annotation.P4jLike;
import com.partner4java.p4jorm.annotation.P4jNoCopy;
import com.partner4java.p4jorm.annotation.P4jQueryForm;
import com.partner4java.p4jorm.annotation.P4jTransient;

/**
 * 注解工具类
 * 
 * @author partner4java
 * 
 */
public class AnnotationGetHelper {
	private static final String SERIAL_VERSION_UID = "serialVersionUID";

	/** 存放类和注解保存FormBean的对应关系 */
	private static Map<Class, AnnotationFormBean> annotationFormBeans = new ConcurrentHashMap<Class, AnnotationFormBean>();

	/** 存放类和注解保存EntityBean的对应关系 */
	private static Map<Class, AnnotationEntityBean> annotationEntityBeans = new ConcurrentHashMap<Class, AnnotationEntityBean>();

	/**
	 * 获取指定类的注解封装对象
	 * 
	 * @param clazz
	 *            封装查询数据对象
	 * @return
	 */
	public static AnnotationFormBean getAnnotationFormBean(Class clazz) {
		if (clazz != null) {
			if (annotationFormBeans.containsKey(clazz)) {
				return annotationFormBeans.get(clazz);
			}

			AnnotationFormBean annotationBean = new AnnotationFormBean();
			annotationBean.setClazz(clazz);

			// 获取Query
			P4jQueryForm query = AnnotationUtils.findAnnotation(clazz, P4jQueryForm.class);
			if (query != null) {
				annotationBean.setQueryForm(query);
			}

			// 遍历所有的field
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				annotationBean.addLike(field, AnnotationUtils.getAnnotation(field, P4jLike.class));
				annotationBean.addGeneral(field, AnnotationUtils.getAnnotation(field, P4jGeneral.class));
				annotationBean.addNoCopy(field, AnnotationUtils.getAnnotation(field, P4jNoCopy.class));
				annotationBean.addAes(field, AnnotationUtils.getAnnotation(field, P4jAES.class));
			}
			
			@SuppressWarnings("rawtypes")
			Class superclass = clazz.getSuperclass();
			while(true){
				if (superclass != null) {
					Field[] superFields = superclass.getDeclaredFields();
					if (superFields != null && superFields.length > 0) {
						for (Field field : superFields) {
							annotationBean.addLike(field, AnnotationUtils.getAnnotation(field, P4jLike.class));
							annotationBean.addGeneral(field, AnnotationUtils.getAnnotation(field, P4jGeneral.class));
							annotationBean.addNoCopy(field, AnnotationUtils.getAnnotation(field, P4jNoCopy.class));
							annotationBean.addAes(field, AnnotationUtils.getAnnotation(field, P4jAES.class));
						}
					}
					superclass = superclass.getSuperclass();
				}else{
					break;
				}
			}

			annotationFormBeans.put(clazz, annotationBean);
			return annotationBean;
		}
		return null;
	}

	/**
	 * 获取表的名称
	 * 
	 * @param clazz
	 * @return
	 */
	public static String getTableName(Class clazz) {
		AnnotationEntityBean annotationEntityBean = getAnnotationEntityBean(clazz);
		P4jEntity entityAnn = annotationEntityBean.getEntity();
		return SQLSpellHelper.getTableName(entityAnn.tableName(), annotationEntityBean.getClazz());
	}

	/**
	 * 获取指定类的注解封装对象
	 * 
	 * @param clazz
	 *            简单实体对象
	 * @return 如果clazz为null或者无P4jEntity注解标识,返回null
	 */
	public static AnnotationEntityBean getAnnotationEntityBean(Class clazz) {
		if (clazz != null) {
			if (annotationEntityBeans.containsKey(clazz)) {
				return annotationEntityBeans.get(clazz);
			}

			// 获取Query
			P4jEntity entity = AnnotationUtils.findAnnotation(clazz, P4jEntity.class);
			// if (entity != null) {
			AnnotationEntityBean annotationEntityBean = new AnnotationEntityBean();
			annotationEntityBean.setEntity(entity);
			annotationEntityBean.setClazz(clazz);

			// 遍历所有的field
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				// 首先不应该是序列化字段
				if (!SERIAL_VERSION_UID.equals(field.getName())) {
					P4jTransient transientAnn = AnnotationUtils.getAnnotation(field, P4jTransient.class);
					// 判断是否指定了不映射
					if (transientAnn == null) {
						AnnotationColumnBean annotationColumnBean = new AnnotationColumnBean();
						annotationColumnBean.setField(field);

						// 是否为主键id
						P4jId idAnn = AnnotationUtils.getAnnotation(field, P4jId.class);
						if (idAnn != null) {
							annotationColumnBean.setIsId(true);
							annotationColumnBean.setAutoIncrement(idAnn.autoIncrement());
						}

						String columnName = null;
						if (idAnn != null) {
							columnName = idAnn.columnName();
						} else {
							// 设置ColumnName
							P4jColumn columnAnn = AnnotationUtils.getAnnotation(field, P4jColumn.class);
							if (columnAnn != null) {
								columnName = columnAnn.columnName();
							}
						}

						// 设置最终的字段名称
						annotationColumnBean.setColumnName(SQLSpellHelper.getColumnName(columnName, field));

						// 设置加密
						P4jAES p4jAES = AnnotationUtils.getAnnotation(field, P4jAES.class);
						if (p4jAES != null) {
							annotationColumnBean.setAes(true);
							annotationColumnBean.setAes(p4jAES.value());
						}

						annotationEntityBean.getAnnotationColumnBeans().add(annotationColumnBean);
					}
				}
			}
			annotationEntityBeans.put(clazz, annotationEntityBean);
			return annotationEntityBean;
			// }

		}
		return null;
	}

}
