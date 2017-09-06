package com.partner4java.p4jorm.dao;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.partner4java.p4jorm.annotation.AnnotationFormBean;
import com.partner4java.p4jorm.bean.page.PageData;
import com.partner4java.p4jorm.bean.page.PageIndex;
import com.partner4java.p4jorm.dao.enums.OrderType;
import com.partner4java.p4jorm.utils.AnnotationGetHelper;
import com.partner4java.p4jorm.utils.FormBean;
import com.partner4java.p4jorm.utils.SQLSpellHelper;
import com.partner4java.p4jtools.generic.GenericsGetHelper;
import com.partner4java.p4jtools.type.NumberHandle;

/**
 * jpa底层dao封装实现<br/>
 * <b>需要资源：</b><br/>
 * 1\EntityManager，提供了setEntityManager方法，对其声明了@PersistenceContext(
 * 若没有开启spring的注解注入,请自行赋值)
 * 
 * @author partner4java
 * 
 * @param <T>
 *            实体类
 * @see P4jDao
 */
public abstract class P4jJpaDaoSupport<T> implements P4jDao<T> {
	@SuppressWarnings("unchecked")
	protected Class<T> entityClass = GenericsGetHelper
			.getSuperGenericsClass(this.getClass());
	@PersistenceContext
	protected EntityManager em;

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	@Override
	public void clear() {
		em.clear();
	}

	@Override
	public void delete(Serializable... entityids) {
		for (Serializable id : entityids) {
			em.remove(em.getReference(entityClass, correctIdType(id)));
		}
	}

	@Override
	public T get(Serializable entityId) {
		if (entityId == null)
			throw new IllegalArgumentException(entityClass.getName()
					+ "get 传入空参");
		return em.find(entityClass, correctIdType(entityId));
	}

	/**
	 * 纠正类型<br/>
	 * 目的是为了避免entity类型验证报错
	 * 
	 * @param id
	 * @return
	 */
	public Object correctIdType(Serializable id) {
		String idStr = id + "";
		if (id != null && NumberHandle.isInteger(idStr)) {
			if (idStr.length() < 10) {
				return new Integer(idStr);
			}

			if (idStr.length() > 10 && idStr.length() < 20) {
				return new Long(idStr);
			}
		}
		return id;
	}

	/**
	 * 保存实体
	 * 
	 * @param entity
	 *            实体
	 * @return 返回持久化后对象(如果对象不为Serializable，返回null)
	 */
	@Override
	public T save(T entity) {
		em.persist(entity);
		return entity;
	}

	@Override
	public void update(T entity) {
		em.merge(entity);
	}

	@Override
	public long getCount(Object formbean) {
		if (formbean != null) {
			AnnotationFormBean annotationBean = AnnotationGetHelper
					.getAnnotationFormBean(formbean.getClass());
			if (annotationBean != null) {
				return doGetTotalCount(formbean, annotationBean);
			}
		}

		Query query = em.createQuery(SELECT_COUNT_FROM
				+ getEntityName(entityClass));
		return (Long) query.getSingleResult();

	}

	@Override
	public PageData<T> query(Object formbean, PageIndex pageIndex,
			LinkedHashMap<String, OrderType> orderby) {
		AnnotationFormBean annotationBean = null;
		if (formbean != null) {
			annotationBean = AnnotationGetHelper.getAnnotationFormBean(formbean
					.getClass());
		}

		PageData<T> pageData = new PageData<T>();
		if (pageIndex != null) {
			pageData.setPageIndex(pageIndex);
		} else {
			pageData.setPageIndex(new PageIndex());
		}

		pageData.setResultlist(doGetList(formbean, annotationBean, pageData,
				orderby));
		pageData.setTotalCount(doGetTotalCount(formbean, annotationBean));

		return pageData;
	}

	@Override
	public List<T> getList(Map<String, Object> parameters,
			LinkedHashMap<String, OrderType> orderby) {
		return getList("*", parameters, orderby, null);
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
	protected long doGetTotalCount(Object formbean,
			AnnotationFormBean annotationBean) {
		StringBuilder builder = new StringBuilder();
		builder.append(SELECT).append(COUNT).append(FROM)
				.append(getEntityName(entityClass)).append(O);

		Query query;
		if (formbean != null && annotationBean != null) {
			// 如果传入了查询数据封装携带对象
			// 获取where
			List<Object> params = new LinkedList<Object>();
			String sql = SQLSpellHelper.getHibernateWhereSQL(annotationBean,
					formbean, params);
			if (sql != null && !"".equals(sql)) {
				builder.append(WHERE).append(sql);
			}

			// 查询赋值
			query = em.createQuery(builder.toString());
			for (int i = 0; i < params.size(); i++) {
				query.setParameter(i + 1, params.get(i));
			}
		} else {
			query = em.createQuery(builder.toString());
		}

		return (Long) query.getSingleResult();
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
	protected List<T> doGetList(Object formbean,
			AnnotationFormBean annotationBean, PageData<T> pageData,
			LinkedHashMap<String, OrderType> orderby) {
		// 封装SQL
		StringBuilder builder = new StringBuilder();
		String returnField = null;
		if (annotationBean != null) {
			returnField = SQLSpellHelper.getReturnField(annotationBean);
		} else {
			returnField = O;
		}

		builder.append(SELECT).append(returnField).append(FROM)
				.append(getEntityName(entityClass)).append(O);

		Query query;
		// 如果传入了查询数据封装携带对象
		if (formbean != null && annotationBean != null) {
			// 获取where
			List<Object> params = new LinkedList<Object>();
			String sql = SQLSpellHelper.getHibernateWhereSQL(annotationBean,
					formbean, params);
			if (sql != null && !"".equals(sql)) {
				builder.append(WHERE).append(sql);
			}

			builder.append(SQLSpellHelper.getOrderBySQL(orderby));

			// 查询赋值
			query = em.createQuery(builder.toString());
			for (int i = 0; i < params.size(); i++) {
				query.setParameter(i + 1, params.get(i));
			}
		} else {
			builder.append(SQLSpellHelper.getOrderBySQL(orderby));
			query = em.createQuery(builder.toString());
		}

		query.setFirstResult((pageData.getPageIndex().getCurrentPage() - 1)
				* pageData.getPageIndex().getMaxResult());
		query.setMaxResults(pageData.getPageIndex().getMaxResult());

		@SuppressWarnings("unchecked")
		List<T> list = query.getResultList();
		return list;
	}

	/**
	 * 获取实体的名称<br/>
	 * 避免用户使用了Entity另起了别名
	 * 
	 * @param <F>
	 * @param clazz
	 *            实体类
	 * @return
	 */
	protected static <F> String getEntityName(Class<F> clazz) {
		String entityname = clazz.getName();
		Entity entity = clazz.getAnnotation(Entity.class);

		// 这里没有判断entity的空指针，因为jpa不会允许没有entity指定
		if (entity.name() != null && !"".equals(entity.name())) {
			entityname = entity.name();
		}
		return entityname;
	}

	@Override
	public List<T> getList(String returnField, Map<String, Object> parameters,
			LinkedHashMap<String, OrderType> orderby, PageIndex pageIndex) {
		throw new RuntimeException("没有实现");
	}

	@Override
	public List<T> getList(String returnField, String sql,
			Map<String, Object> parameters,
			LinkedHashMap<String, OrderType> orderby, PageIndex pageIndex) {
		throw new RuntimeException("没有实现");
	}
	

	@Override
	public void updateChange(T entity, Set<String> changes) {
		throw new RuntimeException("没有实现");
	}

	@Override
	public <C> List<C> getList(String sql, Map<String, Object> parameters,
			Class<C> clazz) {
		throw new RuntimeException("没有实现");
	}

	@Override
	public long getCount(String sql, Map<String, Object> parameters) {
		throw new RuntimeException("没有实现");
	}

	@Override
	public PageData<T> queryR(FormBean formbean, PageIndex pageIndex,
			LinkedHashMap<String, OrderType> orderby) {
		return query(formbean, pageIndex, orderby);
	}

	protected static final String O = " o ";
	protected static final String WHERE = " where ";
	protected static final String FROM = " from ";
	protected static final String SELECT = "select ";
	protected static final String COUNT = "count(o)";
	protected static final String SELECT_FROM = SELECT + COUNT + FROM;
	protected static final String SELECT_COUNT_FROM = "select count(*) from ";

}
