package com.partner4java.p4jorm.utils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jdbc.core.RowMapper;

import com.partner4java.p4jorm.annotation.AnnotationColumnBean;
import com.partner4java.p4jorm.annotation.AnnotationEntityBean;
import com.partner4java.p4jorm.annotation.AnnotationFormBean;
import com.partner4java.p4jorm.annotation.P4jAES;
import com.partner4java.p4jorm.annotation.P4jEntity;
import com.partner4java.p4jorm.annotation.P4jGeneral;
import com.partner4java.p4jorm.annotation.P4jLike;
import com.partner4java.p4jorm.annotation.P4jQueryForm;
import com.partner4java.p4jorm.bean.page.PageIndex;
import com.partner4java.p4jorm.dao.enums.GeneralQueryType;
import com.partner4java.p4jorm.dao.enums.OrderType;
import com.partner4java.p4jtools.reflection.ReflectionDataHelper;
import com.partner4java.p4jtools.str.AESHelper;

/**
 * HQL、JPQL、JDBC SQL拼写辅助工具类<br/>
 * 如果方法命名中没有明确指定拼写具体类型SQL则适用于所有类型SQL拼写；若已指明如getJdbcWhereSQL，即只适用于Jdbc。
 * 
 * @author partner4java
 * 
 */
public class SQLSpellHelper {
	private static Log logger = LogFactory.getLog(SQLSpellHelper.class);

	/**
	 * 返回Jdbc语句的WHERE部分，不包含Where。<br/>
	 * 条件默认以“o”为别名
	 * 
	 * @param annotationBean
	 *            自定义注解封装对象
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param parameters
	 *            传回的参数值
	 * @return 不包含Where，parameters传回参数值
	 */
	public static String getJdbcWhereSQL(AnnotationFormBean annotationBean, Object formbean, Map<String, Object> parameters) {
		return doGetWhereSQL(annotationBean, formbean, parameters, JDBC);
	}

	/**
	 * 返回Jdbc语句的WHERE部分，不包含Where。<br/>
	 * 条件默认以“o”为别名
	 * 
	 * @param annotationBean
	 *            自定义注解封装对象
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param parameters
	 *            传回的参数值
	 * @param type
	 *            数据库类型
	 * @return 不包含Where，parameters传回参数值
	 */
	protected static String doGetWhereSQL(AnnotationFormBean annotationBean, Object formbean, Map<String, Object> parameters, int type) {
		if (annotationBean.getQueryForm() == null) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		// 遍历封装普通SQL
		Map<Field, P4jGeneral> generals = annotationBean.getGenerals();
		if (generals.size() > 0) {
			Set<Field> keys = generals.keySet();
			for (Field field : keys) {

				String generalSQL = doGetGeneralWhereSQL(formbean, field, generals.get(field), annotationBean.getQueryForm(), parameters, type);

				if (generalSQL != null) {
					builder.append(AND).append(generalSQL);
				}
			}
		}

		// 遍历封装like SQL
		Map<Field, P4jLike> likes = annotationBean.getLikes();
		if (likes.size() > 0) {
			Set<Field> keys = likes.keySet();
			for (Field field : keys) {
				String likeSQL = doGetLikeWhereSQL(formbean, field, likes.get(field), annotationBean.getQueryForm(), parameters, type);

				if (likeSQL != null) {
					builder.append(AND).append(likeSQL);
				}
			}
		}
		return removeFirstAnd(builder.toString());
	}

	/**
	 * 返回HQL语句的WHERE部分，不包含Where。<br/>
	 * 条件默认以“o”为别名
	 * 
	 * @param annotationBean
	 *            自定义注解封装对象
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param params
	 *            传回的参数值
	 * @return 不包含Where，params传回参数值
	 */
	public static String getHibernateWhereSQL(AnnotationFormBean annotationBean, Object formbean, List<Object> params) {
		Map<String, Object> parameters = new LinkedHashMap<String, Object>();

		String sql = doGetWhereSQL(annotationBean, formbean, parameters, HIBERNATE);
		params.addAll(parameters.values());

		return sql;
	}

	/**
	 * 去除第一个AND
	 * 
	 * @param sql
	 * @return
	 */
	public static String removeFirstAnd(String sql) {
		if (sql != null) {
			return sql.replaceFirst(AND, "");
		}
		return null;
	}

	/**
	 * 返回指定字段的拼写SQL，不包括AND
	 * 
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param field
	 *            要拼写的字段反射类型
	 * @param general
	 *            字段上定义的注解
	 * @param query
	 *            类上声明的Query注解，如果没有传入null
	 * @param parameters
	 *            传回的参数值
	 * @return 如果此字段不符合拼写条件则返回null，parameters传回参数值
	 */
	public static String getGeneralJdbcWhereSQL(Object formbean, Field field, P4jGeneral general, P4jQueryForm query, Map<String, Object> parameters) {
		return doGetGeneralWhereSQL(formbean, field, general, query, parameters, JDBC);
	}

	/**
	 * 返回指定字段的拼写SQL，不包括AND
	 * 
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param field
	 *            要拼写的字段反射类型
	 * @param general
	 *            字段上定义的注解
	 * @param query
	 *            类上声明的Query注解，如果没有传入null
	 * @param params
	 *            传回的参数值
	 * @return 如果此字段不符合拼写条件则返回null，params传回参数值
	 */
	public static String getGeneralHibernateWhereSQL(Object formbean, Field field, P4jGeneral general, P4jQueryForm query, List<Object> params) {
		Map<String, Object> parameters = new LinkedHashMap<String, Object>();

		String sql = doGetGeneralWhereSQL(formbean, field, general, query, parameters, HIBERNATE);
		params.addAll(parameters.values());

		return sql;
	}

	/**
	 * 返回指定字段的拼写SQL，不包括AND
	 * 
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param field
	 *            要拼写的字段反射类型
	 * @param general
	 *            字段上定义的注解
	 * @param query
	 *            类上声明的Query注解，如果没有传入null
	 * @param parameters
	 *            传回的参数值
	 * @param type
	 *            数据库类型
	 * @return 如果此字段不符合拼写条件则返回null，parameters传回参数值
	 */
	protected static String doGetGeneralWhereSQL(Object formbean, Field field, P4jGeneral general, P4jQueryForm query, Map<String, Object> parameters,
			int type) {
		Object value = ReflectionDataHelper.getFieldValue(formbean, field);
		if (logger.isDebugEnabled()) {
			if (value == null) {
				logger.debug(field.getName() + " is null ");
			} else {
				logger.debug(field.getName() + " value type:" + value.getClass().getName());
			}
		}
		// 如果不添加无数据的字段，且字段为null或空
		if (validateFormValue(query, value, general.addEmpty(), general.addZero())) {
			return null;
		}

		value = aesValue(field, value);

		String columnName = getColumnName(general.columnName(), field);

		StringBuilder builder = new StringBuilder();
		builder.append(ALIAS).append(columnName).append(general.generalQueryType().getStr());

		if (type == JDBC) {
			if (general.generalQueryType() == GeneralQueryType.IN) {
				try {
					if (value != null) {
						if (value instanceof String) {
							builder.append(" (").append(value).append(") ");
						} else if (value instanceof String[]) {
							builder.append(" (").append(getIn((String[]) value)).append(") ");
						} else if (value instanceof int[]) {
							builder.append(" (").append(getIn((int[]) value)).append(") ");
						} else if (value instanceof Integer[]) {
							builder.append(" (").append(getIn((Integer[]) value)).append(") ");
						} else if (value instanceof long[]) {
							builder.append(" (").append(getIn((long[]) value)).append(") ");
						} else if (value instanceof Long[]) {
							builder.append(" (").append(getIn((Long[]) value)).append(") ");
						} else if (value instanceof Object[]) {
							builder.append(" (").append(getIn((Object[]) value)).append(") ");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				builder.append(" :").append(field.getName());
			}
		} else {
			if (general.generalQueryType() == GeneralQueryType.IN) {
				builder.append(LEFT_BRACKET).append(PLACE_HOLDER).append(RIGHT_BRACKET);
			} else {
				builder.append(PLACE_HOLDER);
			}

		}

		parameters.put(field.getName(), value);
		return builder.toString();
	}

	private static String getIn(long[] ids) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			builder.append("'");
			builder.append(ids[i]);
			builder.append("'");
			if (i != ids.length - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	private static String getIn(int[] ids) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			builder.append("'");
			builder.append(ids[i]);
			builder.append("'");
			if (i != ids.length - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	private static String getIn(Object[] ids) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			builder.append("'");
			builder.append(ids[i]);
			builder.append("'");
			if (i != ids.length - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	/**
	 * 对value进行加密
	 */
	private static Object aesValue(Field field, Object value) {
		P4jAES p4jAES = AnnotationUtils.getAnnotation(field, P4jAES.class);
		if (p4jAES != null && field.getType() == String.class) {
			value = AESHelper.encrytor(p4jAES.value(), value.toString());
		}
		return value;
	}

	/**
	 * 返回指定字段的拼写SQL，不包括AND
	 * 
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param field
	 *            要拼写的字段反射类型
	 * @param like
	 *            字段上定义的注解
	 * @param query
	 *            类上声明的Query注解，如果没有传入null
	 * @param params
	 *            传回的参数值
	 * @return 如果此字段不符合拼写条件则返回null，params传回参数值
	 */
	public static String getLikeHibernateWhereSQL(Object formbean, Field field, P4jLike like, P4jQueryForm query, List params) {

		Map<String, Object> parameters = new LinkedHashMap<String, Object>();

		String sql = doGetLikeWhereSQL(formbean, field, like, query, parameters, HIBERNATE);
		params.addAll(parameters.values());

		return sql;
	}

	/**
	 * 返回指定字段的拼写SQL，不包括AND
	 * 
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param field
	 *            要拼写的字段反射类型
	 * @param like
	 *            字段上定义的注解
	 * @param query
	 *            类上声明的Query注解，如果没有传入null
	 * @param parameters
	 *            传回的参数值
	 * @return 如果此字段不符合拼写条件则返回null，parameters传回参数值
	 */
	public static String getLikeJdbcWhereSQL(Object formbean, Field field, P4jLike like, P4jQueryForm query, Map<String, Object> parameters) {
		return doGetLikeWhereSQL(formbean, field, like, query, parameters, JDBC);
	}

	/**
	 * 返回指定字段的拼写SQL，不包括AND
	 * 
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param field
	 *            要拼写的字段反射类型
	 * @param like
	 *            字段上定义的注解
	 * @param query
	 *            类上声明的Query注解，如果没有传入null
	 * @param parameters
	 *            传回的参数值
	 * @param type
	 *            数据库类型
	 * @return 如果此字段不符合拼写条件则返回null，parameters传回参数值
	 */
	protected static String doGetLikeWhereSQL(Object formbean, Field field, P4jLike like, P4jQueryForm query, Map<String, Object> parameters, int type) {
		Object value = ReflectionDataHelper.getFieldValue(formbean, field);
		if (logger.isDebugEnabled()) {
			if (value == null) {
				logger.debug(field.getName() + " is null ");
			} else {
				logger.debug(field.getName() + " value type:" + value.getClass().getName());
			}
		}
		// 如果不添加无数据的字段，且字段为null或空
		if (validateFormValue(query, value, like.addEmpty(), like.addZero())) {
			return null;
		}

		value = aesValue(field, value);

		String columnName = getColumnName(like.columnName(), field);

		value = addPlaceHolder(field, like, value);
		parameters.put(columnName, value);

		StringBuilder builder = new StringBuilder();
		builder.append(ALIAS).append(columnName).append(LIKE);

		if (type == JDBC) {
			builder.append(" :").append(columnName);
		} else {
			builder.append(PLACE_HOLDER);
		}

		return builder.toString();
	}

	/**
	 * 为Like查询字段的赋值进行模糊字符串拼写
	 * 
	 * @param field
	 *            要拼写的字段反射类型
	 * @param like
	 *            字段上定义的注解
	 * @param value
	 *            字段赋值
	 * @return
	 */
	public static Object addPlaceHolder(Field field, P4jLike like, Object value) {
		if (like.placeHolder() != null && "".equals(like.placeHolder())) {
			value = "%" + value + "%";
		} else {
			value = like.placeHolder().replace(getColumnName(like.columnName(), field), value.toString());
		}
		return value;
	}

	/**
	 * 判断该字段值是否有效<br/>
	 * 用于实体判断
	 * 
	 * @param query
	 * @param value
	 * @return
	 */
	public static boolean validateValue(P4jQueryForm query, Object value) {
		return (query == null || !query.addEmpty()) && value == null;
	}

	/**
	 * 判断该字段值是否有效<br/>
	 * 用于formbean判断
	 * 
	 * @param query
	 * @param value
	 * @return
	 */
	public static boolean validateFormValue(P4jQueryForm query, Object value, boolean addEmpty, boolean addZero) {
		if ((query == null || !addZero) && value != null) {
			if (value.getClass().getSimpleName().equals("Integer")) {
				if ((Integer) value == 0) {
					return true;
				}
			}
			if (value.getClass().getSimpleName().equals("Long")) {
				if ((Long) value == 0L) {
					return true;
				}
			}
		}
		return (query == null || !addEmpty) && (value == null || "".equals(value));
	}

	/**
	 * 返回查询column字段名称，如果columnName为空，则返回field的名称
	 * 
	 * @param columnName
	 *            注解指定fieldName的值或默认值
	 * @param field
	 *            该字段的反射类型
	 * @return
	 */
	public static String getColumnName(String columnName, Field field) {
		if (columnName == null || "".equals(columnName)) {
			return field.getName();
		}
		return columnName;
	}

	/**
	 * 获取JDBC插入SQL<br/>
	 * 不再对传入的参数进行空指针判断
	 * 
	 * @param entity
	 *            保存实体
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @param parameters
	 *            用于存放参数对应
	 * @return
	 */
	public static String getJdbcInsertSQL(Object entity, AnnotationEntityBean annotationEntityBean, Map<String, Object> parameters) {
		StringBuilder builder = new StringBuilder();
		P4jEntity entityAnn = annotationEntityBean.getEntity();

		builder.append(INSERT_INTO).append(getTableName(entityAnn.tableName(), entity.getClass())).append(LEFT_BRACKET);

		String builderSQL = doGetJdbcInsertSQL(entity, annotationEntityBean, parameters);
		String valueSQL = builderSQL.replace(SPLIT, ADD_HOLDER);

		builder.append(removeFirstSplit(builderSQL));
		builder.append(VALUES);
		builder.append(removeFirstSplit(valueSQL));
		builder.append(RIGHT_BRACKET);

		return builder.toString();
	}

	/**
	 * 返回查询表名称，如果tableName为空，则返回entity的短名称
	 * 
	 * @param tableName
	 *            注解指定tableName的值或默认值
	 * @param clazz
	 *            要保存的实体类型
	 * @return
	 */
	public static String getTableName(String tableName, Class clazz) {
		if (tableName == null || "".equals(tableName)) {
			return clazz.getSimpleName();
		}
		return tableName;
	}

	/**
	 * 去除第一个逗号
	 * 
	 * @param builderSQL
	 * @return
	 */
	public static String removeFirstSplit(String sql) {
		if (sql != null) {
			return sql.replaceFirst(SPLIT, "");
		}
		return null;
	}

	/**
	 * 获取JDBC插入SQL字段拼装部分<br/>
	 * 不再对传入的参数进行空指针判断
	 * 
	 * @param entity
	 *            保存实体
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @param parameters
	 *            用于存放参数对应
	 * @return 每个字段前面都会有逗号分隔符，第一个分隔符也不会去除
	 */
	protected static String doGetJdbcInsertSQL(Object entity, AnnotationEntityBean annotationEntityBean, Map<String, Object> parameters) {
		StringBuilder builder = new StringBuilder();
		P4jEntity entityAnn = annotationEntityBean.getEntity();
		List<AnnotationColumnBean> annotationColumnBeans = annotationEntityBean.getAnnotationColumnBeans();
		boolean addEmpty = entityAnn.addEmpty();

		// 遍历所有字段
		for (AnnotationColumnBean annotationColumnBean : annotationColumnBeans) {

			Field field = annotationColumnBean.getField();
			Object value = ReflectionDataHelper.getFieldValue(entity, field);
			if (validateValue(addEmpty, value)) {
				continue;
			}

			value = aesValue(annotationColumnBean, field, value);

			try {
				// 主键id无值
				if (annotationColumnBean.isId() && Long.valueOf(value.toString()) == 0L) {
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			builder.append(SPLIT).append(annotationColumnBean.getColumnName());
			parameters.put(annotationColumnBean.getColumnName(), value);
		}

		return builder.toString();
	}

	/**
	 * ase加密值
	 * 
	 */
	private static Object aesValue(AnnotationColumnBean annotationColumnBean, Field field, Object value) {
		if (annotationColumnBean.isAes() && field.getType() == String.class) {
			value = AESHelper.encrytor(annotationColumnBean.getAes(), value.toString());
		}
		return value;
	}

	/**
	 * 判断该字段值是否有效
	 * 
	 * @param addEmpty
	 * @param value
	 * @return
	 */
	public static boolean validateValue(boolean addEmpty, Object value) {
		return (!addEmpty && value == null);
	}

	/**
	 * 获取JDBC更新SQL<br/>
	 * 不再对传入的参数进行空指针判断
	 * 
	 * @param entity
	 *            保存实体
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @param parameters
	 *            用于存放参数对应
	 * @param changes
	 *            指定要修改的字段
	 * @return
	 */
	public static String getJdbcUpdateSQL(Object entity, AnnotationEntityBean annotationEntityBean, Map<String, Object> parameters, Set<String> changes) {
		StringBuilder builder = new StringBuilder();
		P4jEntity entityAnn = annotationEntityBean.getEntity();

		builder.append(UPDATE).append(getTableName(entityAnn.tableName(), entity.getClass())).append(SET);

		builder.append(doGetJdbcUpdateSQL(entity, annotationEntityBean, parameters, changes));

		builder.append(getWhereId(entity, annotationEntityBean, parameters));

		return builder.toString();
	}

	/**
	 * 获取JDBC更新SQL字段拼装部分<br/>
	 * 不再对传入的参数进行空指针判断
	 * 
	 * @param entity
	 *            保存实体
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @param parameters
	 *            用于存放参数对应
	 * @param changes
	 *            指定要修改的字段
	 * @return
	 */
	protected static String doGetJdbcUpdateSQL(Object entity, AnnotationEntityBean annotationEntityBean, Map<String, Object> parameters, Set<String> changes) {
		StringBuilder builder = new StringBuilder();
		P4jEntity entityAnn = annotationEntityBean.getEntity();
		List<AnnotationColumnBean> annotationColumnBeans = annotationEntityBean.getAnnotationColumnBeans();
		boolean addEmpty = entityAnn.addEmpty();

		// 遍历所有字段
		for (AnnotationColumnBean columnBean : annotationColumnBeans) {
			// 不是主键id
			if (!columnBean.isId()) {
				String columnName = columnBean.getColumnName();

				// 指定修改字段
				if (changes != null && !changes.contains(columnName)) {
					continue;
				}

				Field field = columnBean.getField();
				Object value = ReflectionDataHelper.getFieldValue(entity, field);
				if (validateValue(addEmpty, value)) {
					continue;
				}

				value = aesValue(columnBean, field, value);

				builder.append(SPLIT).append(columnName).append(EQ).append(columnName);
				parameters.put(columnName, value);
			}
		}

		return removeFirstSplit(builder.toString());
	}

	/**
	 * 获取JDBC更新SQL字段where id部分<br/>
	 * 不再对传入的参数进行空指针判断
	 * 
	 * @param entity
	 *            保存实体
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @param parameters
	 *            用于存放参数对应
	 * @return
	 */
	protected static String getWhereId(Object entity, AnnotationEntityBean annotationEntityBean, Map<String, Object> parameters) {
		StringBuilder builder = new StringBuilder();
		P4jEntity entityAnn = annotationEntityBean.getEntity();
		List<AnnotationColumnBean> annotationColumnBeans = annotationEntityBean.getAnnotationColumnBeans();
		boolean addEmpty = entityAnn.addEmpty();

		// 遍历所有字段
		for (AnnotationColumnBean annotationColumnBean : annotationColumnBeans) {
			// 是主键id
			if (annotationColumnBean.isId()) {
				Field field = annotationColumnBean.getField();
				Object value = ReflectionDataHelper.getFieldValue(entity, field);
				if (validateValue(addEmpty, value)) {
					continue;
				}

				String columnName = annotationColumnBean.getColumnName();
				builder.append(WHERE).append(columnName).append(EQ).append(columnName);
				parameters.put(columnName, value);
			}
		}

		// 如果没有有效的id是不允许执行更新的
		if (builder.toString() == null || "".equals(builder.toString())) {
			throw new IllegalArgumentException(entity + ERROR);
		}

		return builder.toString();
	}

	/**
	 * 获取删除语句
	 * 
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @param id
	 *            主键id
	 * @param parameters
	 *            携带对应数据
	 * @return
	 */
	public static String getJdbcDeleteSQL(AnnotationEntityBean annotationEntityBean, Serializable id, Map<String, Object> parameters) {
		return doGetJdbcSQLById(annotationEntityBean, id, parameters, DELETE);
	}

	/**
	 * 获取查询主键语句
	 * 
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @param id
	 *            主键id
	 * @param parameters
	 *            携带对应数据
	 * @return
	 */
	public static String getJdbcGetSQL(AnnotationEntityBean annotationEntityBean, Serializable id, Map<String, Object> parameters) {
		return doGetJdbcSQLById(annotationEntityBean, id, parameters, GET);
	}

	/**
	 * 获取查询或删除主键语句
	 * 
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @param id
	 *            主键id
	 * @param parameters
	 *            携带对应数据
	 * @return
	 */
	protected static String doGetJdbcSQLById(AnnotationEntityBean annotationEntityBean, Serializable id, Map<String, Object> parameters, int type) {
		if (id == null) {
			throw new IllegalArgumentException(ERROR);
		}

		StringBuilder builder = new StringBuilder();
		P4jEntity entityAnn = annotationEntityBean.getEntity();
		String fieldName = getIdFieldName(annotationEntityBean);
		if (type == 1) {
			builder.append(DELETE_FROM);
		} else {
			builder.append(SELECT_FROM);
		}

		builder.append(getTableName(entityAnn.tableName(), annotationEntityBean.getClazz())).append(WHERE).append(fieldName).append(EQ).append(fieldName);
		parameters.put(fieldName, id);

		return builder.toString();
	}

	/**
	 * 获取主键column名称
	 * 
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @return
	 */
	public static String getIdFieldName(AnnotationEntityBean annotationEntityBean) {
		List<AnnotationColumnBean> annotationColumnBeans = annotationEntityBean.getAnnotationColumnBeans();
		for (AnnotationColumnBean annotationColumnBean : annotationColumnBeans) {
			if (annotationColumnBean.isId()) {
				return getColumnName(annotationColumnBean.getColumnName(), annotationColumnBean.getField());
			}
		}
		return null;
	}

	/**
	 * 获取JdbcTemplate所需的RowMapper
	 * 
	 * @param annotationEntityBean
	 *            注解解析数据保存类
	 * @param returnField
	 *            指定返回字段，如果返回所有，赋null或*
	 * 
	 * @return
	 */
	public static RowMapper getRowMapper(final AnnotationEntityBean annotationEntityBean, final String returnField) {
		return new RowMapper() {

			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				String[] returnFields = null;
				if (StringUtils.isNotEmpty(returnField) && !"*".equals(returnField)) {
					returnFields = returnField.split(",");
				}

				Class clazz = annotationEntityBean.getClazz();
				Object obj = null;
				try {
					// 实例化对象
					obj = clazz.newInstance();
					List<AnnotationColumnBean> annotationColumnBeans = annotationEntityBean.getAnnotationColumnBeans();
					// 遍历所有实体有效字段
					for (AnnotationColumnBean columnBean : annotationColumnBeans) {
						Field field = columnBean.getField();

						try {
							if (returnFields != null && returnFields.length > 0) {
								for (String mReturnField : returnFields) {
									if (mReturnField.contains(" as ")) {
										mReturnField = mReturnField.split(" as ")[1];
									}
									if (columnBean.getColumnName().equals(mReturnField.trim())) {
										// 挨个设置字段值
										obj = ReflectionDataHelper.setFieldValue(obj, field, deField(rs, columnBean, field));
										break;
									}
								}
							} else {
								// 挨个设置字段值
								obj = ReflectionDataHelper.setFieldValue(obj, field, deField(rs, columnBean, field));
							}
						} catch (Exception e) {
							logger.error(annotationEntityBean.getClazz() + NO_CLOUMN_MAP + e.getMessage());
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				return obj;
			}

		};
	}

	private static Object deField(ResultSet rs, AnnotationColumnBean columnBean, Field field) throws SQLException {
		Object value = rs.getObject(columnBean.getColumnName());
		if (columnBean.isAes() && value != null && field.getType() == String.class) {
			value = AESHelper.decryptor(columnBean.getAes(), value.toString());
		}
		return value;
	}

	/**
	 * 判断获取返回字段<br/>
	 * 如果返回字段为“*”则替换为“o”，只适用于P4jORM的HQL、JPQL拼写
	 * 
	 * @param annotationBean
	 * @return
	 */
	public static String getReturnField(AnnotationFormBean annotationBean) {
		P4jQueryForm queryAnn = annotationBean.getQueryForm();
		if (annotationBean != null && queryAnn == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Note: You don't have set annotation P4jQueryForm for you class　" + annotationBean.getClazz());
			}
			return O;
		}

		String returnField = queryAnn.returnField();
		if ("*".equals(returnField)) {
			returnField = O;
		}
		return returnField;
	}

	/**
	 * 拼写排序SQL
	 * 
	 * @param orderby
	 * @return 返回order by部分SQL
	 */
	public static String getOrderBySQL(LinkedHashMap<String, OrderType> orderby) {
		StringBuffer sql = new StringBuffer("");
		if (orderby != null && orderby.size() > 0) {
			sql.append(ORDER_BY);
			for (String key : orderby.keySet()) {
				sql.append(ALIAS).append(key).append(" ").append(orderby.get(key).getStr()).append(SPLIT);
			}
			sql.deleteCharAt(sql.length() - 1);
		}
		return sql.toString();
	}

	/**
	 * 获取分页SQL
	 * 
	 * @param pageIndex
	 * @param parameters
	 * @return
	 */
	public static String getJdbcLimit(PageIndex pageIndex, Map<String, Object> parameters) {
		parameters.put(FIRST_RESULT, (pageIndex.getCurrentPage() - 1) * pageIndex.getMaxResult());
		parameters.put(MAX_RESULTS, pageIndex.getMaxResult());
		return LIMIT;
	}

	protected static final String MAX_RESULTS = "maxResults";
	protected static final String FIRST_RESULT = "firstResult";
	protected static final String LIMIT = " limit :firstResult, :maxResults";
	protected static final String ORDER_BY = " order by ";
	protected static final String O = " o ";
	private static final int DELETE = 1;
	private static final int GET = 2;
	protected static final String ALIAS = "o.";
	protected static final String AND = " and ";
	protected static final String LIKE = " like ";
	protected static final String PLACE_HOLDER = " ? ";
	protected static final String RIGHT_BRACKET = ")";
	protected static final String VALUES = ") values (";
	protected static final String ADD_HOLDER = ", :";
	protected static final String LEFT_BRACKET = " (";
	protected static final String INSERT_INTO = "insert into ";
	protected static final String SPLIT = ",";
	protected static final String EQ = "= :";
	protected static final String SET = " set ";
	protected static final String UPDATE = "update ";
	protected static final String ERROR = "传入的id为null，或没有添加id注解";
	protected static final String DELETE_FROM = "delete from ";
	protected static final String WHERE = " where ";
	protected static final String SELECT_FROM = "select * from ";
	private static final String NO_CLOUMN_MAP = " 字段与数据库存在不匹配,若无对应关系,请添加P4jTransient注解为本字段：";
	private static final int JDBC = 1;
	private static final int HIBERNATE = 2;

}
