package com.partner4java.p4jorm.dao;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.partner4java.p4jorm.bean.page.PageData;
import com.partner4java.p4jorm.bean.page.PageIndex;
import com.partner4java.p4jorm.dao.enums.OrderType;
import com.partner4java.p4jorm.utils.FormBean;

/**
 * 基本功能的封装（底层DAO，可被多种ORM或JDBC封装）<br/>
 * <b>注意：</b>若数据库需要在事务中进行，实现类需要进行事务声明。<br/>
 * (实现类不加@Transactional:会有一个比较严重的问题,如果你的其他类是继承了这个接口的实现了,但是这个接口的实现类本身的实现出现了异常,
 * 是不回滚的 除非,子类重载了dao实现类的方法)<br/>
 * 
 * @author parter4java
 * 
 * @param <T>
 *            实体类
 */
public interface P4jDao<T> {
	/**
	 * 清除一级缓存的数据<br/>
	 * 并非所有ORM都需要，针对如Hibernate
	 */
	public void clear();

	/**
	 * 保存实体
	 * 
	 * @param entity
	 *            实体
	 * @return 返回主键或者保存后的实体对象，不强制要求，由具体实现指定规则（返回为可选项，不强制要求实现返回）
	 */
	public T save(T entity);

	/**
	 * 更新实体
	 * 
	 * @param entity
	 *            实体
	 */
	public void update(T entity);

	/**
	 * 更新指定字段
	 * 
	 * @param entity
	 *            实体
	 * @param changes
	 *            更新字段(数据库字段)
	 */
	public void updateChange(T entity, Set<String> changes);

	/**
	 * 删除实体<br/>
	 * 
	 * @param entityids
	 *            实体id数组
	 */
	public void delete(Serializable... entityids);

	/**
	 * 获取实体
	 * 
	 * @param <T>
	 * @param entityId
	 *            实体id
	 * @return
	 */
	public T get(Serializable entityId);

	/**
	 * 根据指定字段获取单个值
	 * 
	 * @param column
	 *            字段名称
	 * @param value
	 *            字段值
	 * @return 如果多个值会抛出异常
	 */
	public T get(String column, Object value) throws IllegalArgumentException;

	/**
	 * 获取记录总数
	 * 
	 * @param formbean
	 *            查询封装实体(可以为null，当为null时，表示无查询条件)
	 * @return
	 */
	public long getCount(Object formbean);

	/**
	 * 查询个数
	 * 
	 * @param sql
	 *            如果为null，默认map参数全部为等值查询<br/>
	 *            示例：select count(*) from t_user where phone = :phone
	 * @param parameters
	 *            参数值对应
	 * @return
	 */
	public long getCount(String sql, Map<String, Object> parameters);

	/**
	 * 获取查询结果
	 * 
	 * @param returnField
	 *            返回字段
	 * @param parameters
	 *            参数，key为字段名称，value为值
	 * @param orderby
	 *            排序规则，如果为null，不需要排序
	 * @param pageIndex
	 *            分页信息，如果为null，不分页返回查询全部数据
	 * @return
	 */
	public PageData<T> query(String returnField, Map<String, Object> parameters,
			LinkedHashMap<String, OrderType> orderby, PageIndex pageIndex);

	/**
	 * 获取查询结果
	 * 
	 * @param formbean
	 *            查询封装实体(可以为null，当为null时，表示无查询条件)
	 * @param pageIndex
	 *            分页信息(可以为null，但是当传入null时，我们会自动调用PageIndex无参构造器新建一个PageIndex)
	 * @param orderby
	 *            排序规则，key为字段名称，value为排序规则(可以为null，为null表示不拼写任何排序规则)
	 * @return
	 */
	public PageData<T> query(Object formbean, PageIndex pageIndex, LinkedHashMap<String, OrderType> orderby);

	/**
	 * 远程服务获取查询结果
	 * 
	 * @param formbean
	 *            查询封装实体(可以为null，当为null时，表示无查询条件)
	 * @param pageIndex
	 *            分页信息(可以为null，但是当传入null时，我们会自动调用PageIndex无参构造器新建一个PageIndex)
	 * @param orderby
	 *            排序规则，key为字段名称，value为排序规则(可以为null，为null表示不拼写任何排序规则)
	 * @return
	 */
	public PageData<T> queryR(FormBean formbean, PageIndex pageIndex, LinkedHashMap<String, OrderType> orderby);

	/**
	 * 查询数据列表
	 * 
	 * @param returnField
	 *            返回字段
	 * @param parameters
	 *            参数，key为字段名称，value为值
	 * @param orderby
	 *            排序规则，如果为null，不需要排序
	 * @param pageData
	 *            分页信息，如果为null，不分页返回查询全部数据
	 * @return
	 */
	public List<T> getList(String returnField, Map<String, Object> parameters, LinkedHashMap<String, OrderType> orderby,
			PageIndex pageIndex);

	/**
	 * 获取数据列表
	 * 
	 * @param parameters
	 *            参数，key为字段名称，value为值；如果为null，不需要查询字段
	 * @param orderby
	 *            排序规则，如果为null，不需要排序
	 * @return 返回所有字段
	 */
	public List<T> getList(Map<String, Object> parameters, LinkedHashMap<String, OrderType> orderby);

	/**
	 * 根据sql查询数据列表
	 * 
	 * @param returnField
	 *            返回字段
	 * @param sql
	 *            查询条件，只拼写sql内的内容
	 * @param parameters
	 *            参数，key为字段名称，value为值
	 * @param orderby
	 *            排序规则，如果为null，不需要排序
	 * @param pageData
	 *            分页信息，如果为null，不分页返回查询全部数据
	 * @return
	 */
	public List<T> getList(String returnField, String sql, Map<String, Object> parameters,
			LinkedHashMap<String, OrderType> orderby, PageIndex pageIndex);

	/**
	 * 获取查询结果
	 * 
	 * @param sql
	 *            完整sql，自己拼写
	 * @param parameters
	 *            参数，key为字段名称，value为值
	 * @param clazz
	 *            返回对象类型
	 * @return
	 */
	public <C> List<C> getList(String sql, Map<String, Object> parameters, Class<C> clazz);

	/**
	 * 根绝id数组获取数据列表
	 * 
	 * @param ids
	 *            id数组
	 * @return
	 */
	public List<T> getByIds(Object[] ids);

	/**
	 * 获取所有数据，最多返回一万条数据
	 * 
	 * @return
	 */
	public List<T> getAll();
}
