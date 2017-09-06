package com.partner4java.p4jorm.dao;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.partner4java.p4jorm.annotation.AnnotationFormBean;
import com.partner4java.p4jorm.bean.page.PageData;
import com.partner4java.p4jorm.bean.page.PageIndex;
import com.partner4java.p4jorm.dao.enums.OrderType;
import com.partner4java.p4jorm.utils.AnnotationGetHelper;
import com.partner4java.p4jorm.utils.FormBean;
import com.partner4java.p4jorm.utils.SQLSpellHelper;
import com.partner4java.p4jtools.generic.GenericsGetHelper;

/**
 * hibernate底层dao封装实现<br/>
 * <b>需要资源：</b><br/>
 * 1\SessionFactory，提供了setSessionFactory(SessionFactory
 * sessionFactory)方法，对其声明了@Autowired(若没有开启spring的注解注入,请自行赋值)
 * 
 * @author partner4java
 * 
 * @param <T>
 *            实体类
 * @see P4jDao
 */
public abstract class P4jHibernateDaoSupport<T> implements P4jDao<T> {
	@SuppressWarnings("unchecked")
	protected Class<T> entityClass = GenericsGetHelper
			.getSuperGenericsClass(this.getClass());

	protected SessionFactory sessionFactory;

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void clear() {
		sessionFactory.getCurrentSession().clear();
	}

	@Override
	public void delete(Serializable... entityids) {
		for (Serializable id : entityids) {
			sessionFactory.getCurrentSession().delete(
					sessionFactory.getCurrentSession().load(entityClass, id));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(Serializable entityId) {
		return (T) sessionFactory.getCurrentSession()
				.get(entityClass, entityId);
	}

	/**
	 * 保存实体
	 * 
	 * @param entity
	 *            实体
	 * @return 返回主键对象（随不同的场景而定，可能是Integer或Long）
	 */
	@Override
	public T save(T entity) {
		sessionFactory.getCurrentSession().persist(entity);
		return entity;
	}

	@Override
	public void update(T entity) {
		sessionFactory.getCurrentSession().update(entity);
		// sessionFactory.getCurrentSession().merge(entity);// 合并
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

		Query query = sessionFactory.getCurrentSession().createQuery(
				SELECT_COUNT_FROM + getEntityName(entityClass));
		return (Long) query.uniqueResult();

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
		builder.append(SELECT_FROM).append(getEntityName(entityClass))
				.append(O);

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
			query = sessionFactory.getCurrentSession().createQuery(
					builder.toString());
			for (int i = 0; i < params.size(); i++) {
				query.setParameter(i, params.get(i));
			}
		} else {
			query = sessionFactory.getCurrentSession().createQuery(
					builder.toString());
		}

		return (Long) query.uniqueResult();
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
			query = sessionFactory.getCurrentSession().createQuery(
					builder.toString());
			for (int i = 0; i < params.size(); i++) {
				query.setParameter(i, params.get(i));
			}
		} else {
			builder.append(SQLSpellHelper.getOrderBySQL(orderby));
			query = sessionFactory.getCurrentSession().createQuery(
					builder.toString());
		}

		query.setFirstResult((pageData.getPageIndex().getCurrentPage() - 1)
				* pageData.getPageIndex().getMaxResult());
		query.setMaxResults(pageData.getPageIndex().getMaxResult());

		@SuppressWarnings("unchecked")
		List<T> list = query.list();
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
		if (entity != null && entity.name() != null
				&& !"".equals(entity.name())) {
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
		throw new RuntimeException("没有实现");
	}

	protected static final String O = " o ";
	protected static final String WHERE = " where ";
	protected static final String FROM = " from ";
	protected static final String SELECT = "select ";
	protected static final String COUNT = "count(o)";
	protected static final String SELECT_FROM = SELECT + COUNT + FROM;
	protected static final String SELECT_COUNT_FROM = "select count(*) from ";
}
