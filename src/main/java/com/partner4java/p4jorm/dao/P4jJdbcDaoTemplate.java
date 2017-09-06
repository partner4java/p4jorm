package com.partner4java.p4jorm.dao;

import java.io.Serializable;
import java.util.LinkedHashMap;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.partner4java.p4jorm.bean.page.PageData;
import com.partner4java.p4jorm.bean.page.PageIndex;
import com.partner4java.p4jorm.dao.enums.OrderType;

/**
 * Jdbc底层dao封装实现模板<br/>
 * 可在某一类中创建一个本类的静态唯一实例，供统一调用，或交给Spring IoC容器。<br/>
 * 
 * 目前只支持了MySQL；在对实体类进行映射指定时，建议把id进行P4jId注解指定； <b>需要资源：</b><br/>
 * 1\NamedParameterJdbcTemplate，提供了唯一的namedParameterJdbcTemplate构造器
 * 
 * @author partner4java
 * 
 * @param <T>
 *            实体类
 * @see P4jDao
 */
public class P4jJdbcDaoTemplate {
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private P4jJdbcDaoSupport<Object> dao = new P4jJdbcDaoSupport<Object>() {
	};

	public P4jJdbcDaoTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		dao.setNamedParameterJdbcTemplate(namedParameterJdbcTemplate);
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}

	/**
	 * 保存实体
	 * 
	 * @param entity
	 *            实体
	 * @return 返回null
	 */
	public Serializable save(Object entity) {
		try {
			dao.doUpdate(entity, dao.INSERT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 更新实体<br/>
	 * 只提供根据id进行更新
	 * 
	 * @param entity
	 *            实体
	 */
	public void update(Object entity) {
		dao.doUpdate(entity, dao.UPDATE);
	}

	/**
	 * 删除实体<br/>
	 * 
	 * @param entityClass
	 *            实体类
	 * @param entityids
	 *            实体id数组
	 */
	public void delete(Class entityClass, Serializable... entityids) {
		dao.doDelete(entityClass, entityids);
	}

	/**
	 * 获取实体
	 * 
	 * @param <T>
	 * @param entityClass
	 *            实体类
	 * @param entityId
	 *            实体id
	 * @return
	 */
	public <T> T get(Class<T> entityClass, Serializable entityId) {
		return (T) dao.doGet(entityClass, entityId);
	}

	/**
	 * 获取记录总数
	 * 
	 * @param entityClass
	 *            实体类
	 * @param formbean
	 *            查询封装实体(可以为null，当为null时，表示无查询条件)
	 * @return
	 */
	public long getCount(Class entityClass, Object formbean) {
		return dao.doGetCount(entityClass, formbean);
	}

	/**
	 * 获取查询结果
	 * 
	 * @param entityClass
	 *            返回实体类
	 * @param formbean
	 *            查询封装实体(可以为null，当为null时，表示无查询条件)
	 * @param pageIndex
	 *            分页信息(可以为null，但是当传入null时，我们会自动调用PageIndex无参构造器新建一个PageIndex)
	 * @param orderby
	 *            排序规则，key为字段名称，value为排序规则(可以为null，为null表示不拼写任何排序规则)
	 * @return
	 */
	public <T> PageData<T> query(Class<T> entityClass, Object formbean, PageIndex pageIndex,
			LinkedHashMap<String, OrderType> orderby) {
		return (PageData<T>) dao.doQuery(entityClass, formbean, pageIndex, orderby);
	}

}
