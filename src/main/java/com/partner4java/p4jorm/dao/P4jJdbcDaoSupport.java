package com.partner4java.p4jorm.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.partner4java.p4jorm.annotation.AnnotationColumnBean;
import com.partner4java.p4jorm.annotation.AnnotationEntityBean;
import com.partner4java.p4jorm.annotation.AnnotationFormBean;
import com.partner4java.p4jorm.bean.page.PageData;
import com.partner4java.p4jorm.bean.page.PageIndex;
import com.partner4java.p4jorm.dao.enums.OrderType;
import com.partner4java.p4jorm.utils.AnnotationGetHelper;
import com.partner4java.p4jorm.utils.FormBean;
import com.partner4java.p4jorm.utils.SQLSpellHelper;
import com.partner4java.p4jtools.IDGenerate;
import com.partner4java.p4jtools.generic.GenericsGetHelper;
import com.partner4java.p4jtools.str.AESHelper;

/**
 * Jdbc底层dao封装实现<br/>
 * 目前只支持了MySQL；在对实体类进行映射指定时，建议把id进行P4jId注解指定； <b>需要资源：</b><br/>
 * 1\NamedParameterJdbcTemplate，提供了setNamedParameterJdbcTemplate(
 * NamedParameterJdbcTemplate
 * namedParameterJdbcTemplate)方法，对其声明了@Autowired(若没有开启spring的注解注入,请自行赋值)
 * 
 * @author partner4java
 * 
 * @param <T>
 *            实体类
 * @see P4jDao
 */
public abstract class P4jJdbcDaoSupport<T> implements P4jDao<T> {
	private static final String GET_NULL_ERROR = "P4jJdbcDaoSupport.get 传入空id";

	@SuppressWarnings("unchecked")
	protected Class<T> entityClass = GenericsGetHelper.getSuperGenericsClass(this.getClass());
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	@Qualifier("namedParameterJdbcTemplate")
	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
	}

	/**
	 * 保存实体
	 * 
	 * @param entity
	 *            实体
	 * @return 返回null
	 */
	@Override
	public T save(T entity) {
		doUpdate(entity, INSERT);
		return entity;
	}

	/**
	 * 更新实体<br/>
	 * 只提供根据id进行更新
	 * 
	 * @param entity
	 *            实体
	 */
	@Override
	public void update(T entity) {
		doUpdate(entity, UPDATE);
	}

	@Override
	public void updateChange(T entity, Set<String> changes) {
		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entity.getClass());
		if (annotationEntityBean != null) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			String sql = SQLSpellHelper.getJdbcUpdateSQL(entity, annotationEntityBean, parameters, changes);
			namedParameterJdbcTemplate.update(sql, parameters);
		}

	}

	@Override
	public void delete(Serializable... entityids) {
		doDelete(entityClass, entityids);
	}

	protected void doDelete(Class entityClass, Serializable... entityids) {
		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);
		for (Serializable id : entityids) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			String sql = SQLSpellHelper.getJdbcDeleteSQL(annotationEntityBean, id, parameters);
			namedParameterJdbcTemplate.update(sql, parameters);
		}
	}

	@Override
	public T get(Serializable entityId) {
		try {
			return doGet(entityClass, entityId);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}

	@Override
	public T get(String column, Object value) throws IllegalArgumentException {
		Map<String, Object> parameters = new LinkedHashMap<String, Object>();
		parameters.put(column, value);
		List<T> list = this.getList(parameters, null);
		if (list != null) {
			if (list.size() > 1) {
				throw new IllegalArgumentException("包含多个值");
			}
			if (list.size() == 1) {
				return list.get(0);
			}
		}

		return null;
	}

	protected T doGet(Class entityClass, Serializable entityId) {
		if (entityId == null) {
			throw new IllegalArgumentException(GET_NULL_ERROR);
		}

		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);
		Map<String, Object> parameters = new HashMap<String, Object>();

		String sql = SQLSpellHelper.getJdbcGetSQL(annotationEntityBean, entityId, parameters);

		RowMapper rowMapper = SQLSpellHelper.getRowMapper(annotationEntityBean, null);

		return (T) namedParameterJdbcTemplate.queryForObject(sql, parameters, rowMapper);
	}

	@Override
	public long getCount(Object formbean) {
		return doGetCount(entityClass, formbean);
	}

	protected long doGetCount(Class entityClass, Object formbean) {
		// 如果存在formbean
		if (formbean != null) {
			AnnotationFormBean annotationBean = AnnotationGetHelper.getAnnotationFormBean(formbean.getClass());
			if (annotationBean != null) {
				return doGetTotalCount(entityClass, formbean, annotationBean);
			}
		}

		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);
		return namedParameterJdbcTemplate.queryForObject(
				SELECT_COUNT_FROM
						+ SQLSpellHelper.getTableName(annotationEntityBean.getEntity().tableName(), entityClass),
				new HashMap<String, Object>(), Long.class);
	}

	@Override
	public PageData<T> query(String returnField, Map<String, Object> parameters,
			LinkedHashMap<String, OrderType> orderby, PageIndex pageIndex) {
		PageData<T> pageData = new PageData<T>();
		if (pageIndex != null) {
			pageData.setPageIndex(pageIndex);
		} else {
			pageData.setPageIndex(new PageIndex());
		}
		
		pageData.setTotalCount(getCount(null, parameters));
		pageData.setResultlist(getList(returnField, parameters, orderby, pageIndex));

		return pageData;
	}

	@Override
	public PageData<T> query(Object formbean, PageIndex pageIndex, LinkedHashMap<String, OrderType> orderby) {
		return doQuery(entityClass, formbean, pageIndex, orderby);
	}

	@Override
	public PageData<T> queryR(FormBean formbean, PageIndex pageIndex, LinkedHashMap<String, OrderType> orderby) {
		PageData<T> pageData = new PageData<T>();
		if (pageIndex != null) {
			pageData.setPageIndex(pageIndex);
		} else {
			pageData.setPageIndex(new PageIndex());
		}

		pageData.setResultlist(doGetList(entityClass, formbean, pageData, orderby));
		pageData.setTotalCount(doGetTotalCount(entityClass, formbean));

		return pageData;
	}

	/**
	 * 获取当前查询分页前所有数据量
	 * 
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @return
	 */
	protected long doGetTotalCount(Class entityClass, FormBean formbean) {
		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);

		StringBuilder builder = new StringBuilder();

		builder.append(SELECT_COUNT_FROM)
				.append(SQLSpellHelper.getTableName(annotationEntityBean.getEntity().tableName(), entityClass))
				.append(O);

		String sql = formbean.getSql();
		if (sql != null && !"".equals(sql)) {
			builder.append(WHERE).append(sql);
		}

		return namedParameterJdbcTemplate.queryForObject(builder.toString(), formbean.getParams(), Long.class);
	}

	/**
	 * 获取列表数据
	 * 
	 * @return
	 */
	protected List<T> doGetList(Class entityClass, FormBean formbean, PageData<T> pageData,
			LinkedHashMap<String, OrderType> orderby) {
		// 封装SQL
		StringBuilder builder = new StringBuilder();
		String returnField = formbean.getReturnFiled();

		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);

		builder.append(SELECT).append(returnField).append(FROM)
				.append(SQLSpellHelper.getTableName(annotationEntityBean.getEntity().tableName(), entityClass))
				.append(O);

		// 获取where
		Map<String, Object> parameters = formbean.getParams();

		// 如果传入了查询数据封装携带对象
		if (formbean != null) {
			String sql = formbean.getSql();
			if (sql != null && !"".equals(sql)) {
				builder.append(WHERE).append(sql);
			}
		}

		builder.append(SQLSpellHelper.getOrderBySQL(orderby));

		if (!builder.toString().contains("order") && StringUtils.isNotEmpty(formbean.getOrderBy())) {
			builder.append(" order by ").append(formbean.getOrderBy());
		}

		builder.append(SQLSpellHelper.getJdbcLimit(pageData.getPageIndex(), parameters));

		RowMapper rowMapper = SQLSpellHelper.getRowMapper(annotationEntityBean, "*");

		return namedParameterJdbcTemplate.query(builder.toString(), parameters, rowMapper);
	}

	protected PageData<T> doQuery(Class entityClass, Object formbean, PageIndex pageIndex,
			LinkedHashMap<String, OrderType> orderby) {
		if (formbean != null) {
			AnnotationFormBean annotationBean = AnnotationGetHelper.getAnnotationFormBean(formbean.getClass());
			if (annotationBean != null) {
				PageData<T> pageData = new PageData<T>();
				if (pageIndex != null) {
					pageData.setPageIndex(pageIndex);
				} else {
					pageData.setPageIndex(new PageIndex());
				}

				pageData.setResultlist(doGetList(entityClass, formbean, annotationBean, pageData, orderby));
				pageData.setTotalCount(doGetTotalCount(entityClass, formbean, annotationBean));

				return pageData;
			}
		}
		return null;
	}

	/**
	 * 获取列表数据
	 * 
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param annotationBean
	 *            获取指定类的注解封装对象
	 * @param orderby
	 *            排序规则，key为字段名称，value为排序规则
	 * @return
	 */
	protected List<T> doGetList(Class entityClass, Object formbean, AnnotationFormBean annotationBean,
			PageData<T> pageData, LinkedHashMap<String, OrderType> orderby) {
		// 封装SQL
		StringBuilder builder = new StringBuilder();
		String returnField = annotationBean.getQueryForm().returnField();

		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);

		builder.append(SELECT).append(returnField).append(FROM)
				.append(SQLSpellHelper.getTableName(annotationEntityBean.getEntity().tableName(), entityClass))
				.append(O);

		// 获取where
		Map<String, Object> parameters = new HashMap<String, Object>();

		// 如果传入了查询数据封装携带对象
		if (formbean != null) {
			String sql = SQLSpellHelper.getJdbcWhereSQL(annotationBean, formbean, parameters);
			if (sql != null && !"".equals(sql)) {
				builder.append(WHERE).append(sql);
			}
		}

		builder.append(SQLSpellHelper.getOrderBySQL(orderby));

		if (!builder.toString().contains("order") && StringUtils.isNotEmpty(annotationBean.getQueryForm().orderBy())) {
			builder.append(" order by ").append(annotationBean.getQueryForm().orderBy());
		}

		builder.append(SQLSpellHelper.getJdbcLimit(pageData.getPageIndex(), parameters));

		RowMapper rowMapper = SQLSpellHelper.getRowMapper(annotationEntityBean, "*");

		return namedParameterJdbcTemplate.query(builder.toString(), parameters, rowMapper);
	}

	@Override
	public List<T> getList(String returnField, Map<String, Object> parameters, LinkedHashMap<String, OrderType> orderby,
			PageIndex pageIndex) {// 封装SQL
		StringBuilder builder = new StringBuilder();

		builder.append(SELECT).append(returnField).append(FROM).append(AnnotationGetHelper.getTableName(entityClass))
				.append(O);

		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);
		parameters = desParameters(annotationEntityBean, parameters);

		if (parameters != null && parameters.size() > 0) {
			builder.append(WHERE);
			Set<String> keys = parameters.keySet();
			int i = 0;
			for (String key : keys) {
				if (i != 0) {
					builder.append(" and ");
				}
				i++;
				builder.append(" o.").append(key).append(" = :").append(key);
			}
		}

		if (orderby != null) {
			builder.append(SQLSpellHelper.getOrderBySQL(orderby));
		}

		if (pageIndex != null) {
			if (parameters == null) {
				parameters = new LinkedHashMap<String, Object>();
			}
			builder.append(SQLSpellHelper.getJdbcLimit(pageIndex, parameters));
		}

		RowMapper rowMapper = SQLSpellHelper.getRowMapper(annotationEntityBean, returnField);

		return namedParameterJdbcTemplate.query(builder.toString(), parameters, rowMapper);
	}

	/**
	 * 对参数进行加密<br/>
	 * 判断条件：如果查询条件的占位符和数据库字段名称相同或者和field名称相同
	 */
	private Map<String, Object> desParameters(AnnotationEntityBean annotationEntityBean,
			Map<String, Object> parameters) {
		try {
			if (parameters != null && annotationEntityBean != null
					&& annotationEntityBean.getAnnotationColumnBeans() != null) {
				for (String key : parameters.keySet()) {
					if (parameters.get(key) != null) {
						for (AnnotationColumnBean columnBean : annotationEntityBean.getAnnotationColumnBeans()) {
							// 如果查询条件的占位符和数据库字段名称相同或者和field名称相同
							if ((key.equals(columnBean.getColumnName())
									|| key.toLowerCase().equals(columnBean.getField().getName().toLowerCase()))
									&& columnBean.isAes()) {
								parameters.put(key,
										AESHelper.encrytor(columnBean.getAes(), parameters.get(key).toString()));
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parameters;
	}

	@Override
	public List<T> getList(String returnField, String sql, Map<String, Object> parameters,
			LinkedHashMap<String, OrderType> orderby, PageIndex pageIndex) {
		StringBuilder builder = new StringBuilder();

		builder.append(SELECT).append(returnField).append(FROM).append(AnnotationGetHelper.getTableName(entityClass))
				.append(O);

		if (!StringUtils.isEmpty(sql)) {
			builder.append(WHERE).append(sql);
		}

		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);
		parameters = desParameters(annotationEntityBean, parameters);

		if (orderby != null) {
			builder.append(SQLSpellHelper.getOrderBySQL(orderby));
		}

		if (pageIndex != null) {
			if (parameters == null) {
				parameters = new LinkedHashMap<String, Object>();
			}
			builder.append(SQLSpellHelper.getJdbcLimit(pageIndex, parameters));
		}

		RowMapper rowMapper = SQLSpellHelper.getRowMapper(annotationEntityBean, returnField);

		return namedParameterJdbcTemplate.query(builder.toString(), parameters, rowMapper);
	}

	@SuppressWarnings("deprecation")
	@Override
	public long getCount(String sql, Map<String, Object> parameters) {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.isEmpty(sql)) {
			builder.append(SELECT_COUNT_FROM).append(AnnotationGetHelper.getTableName(entityClass)).append(O);

			AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);
			parameters = desParameters(annotationEntityBean, parameters);

			if (parameters != null && parameters.size() > 0) {
				builder.append(WHERE);
				Set<String> keys = parameters.keySet();
				int i = 0;
				for (String key : keys) {
					if (i != 0) {
						builder.append(" and ");
					}
					i++;
					builder.append(" o.").append(key).append(" = :").append(key);
				}
			}
			sql = builder.toString();
		}

		return namedParameterJdbcTemplate.queryForObject(sql, parameters, Long.class);
	}

	@Override
	public <C> List<C> getList(String sql, Map<String, Object> parameters, Class<C> clazz) {
		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(clazz);
		parameters = desParameters(annotationEntityBean, parameters);

		RowMapper rowMapper = SQLSpellHelper.getRowMapper(annotationEntityBean, null);
		return namedParameterJdbcTemplate.query(sql, parameters, rowMapper);
	}

	@Override
	public List<T> getList(Map<String, Object> parameters, LinkedHashMap<String, OrderType> orderby) {
		return getList("*", parameters, orderby, null);
	}

	@Override
	public List<T> getByIds(Object[] ids) {
		if (ids == null || ids.length > 5000) {
			return null;
		}
		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);
		String fieldName = SQLSpellHelper.getIdFieldName(annotationEntityBean);

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(fieldName).append(" in (").append(getIdIn(ids)).append(") ");
		return getList("*", sqlBuilder.toString(), null, null, null);
	}

	@Override
	public List<T> getAll() {
		PageIndex pageIndex = new PageIndex();
		pageIndex.setMaxResult(10000);
		return getList("*", null, null, pageIndex);
	}

	/**
	 * 获取当前查询分页前所有数据量
	 * 
	 * @param formbean
	 *            添加查询注解、传递查询参数对象
	 * @param annotationBean
	 *            获取指定类的注解封装对象
	 * @return
	 */
	protected long doGetTotalCount(Class entityClass, Object formbean, AnnotationFormBean annotationBean) {
		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entityClass);

		StringBuilder builder = new StringBuilder();

		builder.append(SELECT_COUNT_FROM)
				.append(SQLSpellHelper.getTableName(annotationEntityBean.getEntity().tableName(), entityClass))
				.append(O);

		Query query;
		// 如果传入了查询数据封装携带对象
		// 获取where
		Map<String, Object> parameters = new HashMap<String, Object>();
		String sql = SQLSpellHelper.getJdbcWhereSQL(annotationBean, formbean, parameters);
		if (sql != null && !"".equals(sql)) {
			builder.append(WHERE).append(sql);
		}

		return namedParameterJdbcTemplate.queryForObject(builder.toString(), parameters, Long.class);
	}

	protected void doUpdate(Object entity, int type) {
		AnnotationEntityBean annotationEntityBean = AnnotationGetHelper.getAnnotationEntityBean(entity.getClass());
		if (annotationEntityBean != null) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			String sql;
			if (type == INSERT) {

				// 设置非自增id，手工赋id
				try {
					for (AnnotationColumnBean annotationColumnBean : annotationEntityBean.getAnnotationColumnBeans()) {
						if (annotationColumnBean.isId() && !annotationColumnBean.isAutoIncrement()) {
							Field field = annotationColumnBean.getField();
							if (field.getType() == long.class || field.getType() == Long.class) {
								field.setAccessible(true);
								if (field.getLong(entity) < 1) {
									field.setLong(entity, IDGenerate.getUniqueID());
								}
							}
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				sql = SQLSpellHelper.getJdbcInsertSQL(entity, annotationEntityBean, parameters);
				GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
				namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(parameters), keyHolder);

				// 通过钩子获取数据库自增id的值
				try {
					for (AnnotationColumnBean annotationColumnBean : annotationEntityBean.getAnnotationColumnBeans()) {
						if (annotationColumnBean.isId()) {
							Field field = annotationColumnBean.getField();
							field.setAccessible(true);
							if (field.getType() == int.class || field.getType() == Integer.class) {
								if (field.getInt(entity) < 1) {
									field.setInt(entity, keyHolder.getKey().intValue());
								}
							} else if (field.getType() == long.class || field.getType() == Long.class) {
								if (field.getLong(entity) < 1) {
									if (annotationColumnBean.isAutoIncrement()) {
										field.setLong(entity, keyHolder.getKey().longValue());
									}
								}
							}
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sql = SQLSpellHelper.getJdbcUpdateSQL(entity, annotationEntityBean, parameters, null);
				namedParameterJdbcTemplate.update(sql, parameters);
			}
		}
	}

	/**
	 * 返回in里面的数据组装
	 * 
	 * @param ids
	 * @return
	 */
	protected String getIdIn(Object[] ids) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			builder.append(ids[i]);
			if (i != ids.length - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	protected String getIdIn(long[] ids) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			builder.append(ids[i]);
			if (i != ids.length - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	protected String getIdIn(int[] ids) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			builder.append(ids[i]);
			if (i != ids.length - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	/**
	 * 返回in里面的数据组装
	 * 
	 * @param ids
	 * @return
	 */
	protected String getIdIn(@SuppressWarnings("rawtypes") List ids) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < ids.size(); i++) {
			builder.append(ids.get(i));
			if (i != ids.size() - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	protected static final int INSERT = 1;
	protected static final int UPDATE = 2;
	protected static final String SELECT = "select ";
	protected static final String FROM = " from ";
	protected static final String SELECT_COUNT_FROM = "select count(*) from ";
	protected static final String O = " o ";
	protected static final String WHERE = " where ";
}
