package com.partner4java.p4jorm.annotation;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放类解析后的分析封装数据。<br/>
 * 每一个AnnotationBean都对应一个单独的传入查询对象，所以可对此对象的解析做缓存。<br/>
 * 只存放解析的第一手数据，并不对数据进行加工。
 * 
 * @author partner4java
 * 
 */
public class AnnotationFormBean implements Serializable {
	private static final long serialVersionUID = 6454681824934906084L;

	/** 每个类上标注的Query注解 */
	private P4jQueryForm queryForm;
	/** 类中所有标注Like注解的字段 */
	private Map<Field, P4jLike> likes = new ConcurrentHashMap<Field, P4jLike>();

	/** 类中所有标注General注解的字段 */
	private Map<Field, P4jGeneral> generals = new ConcurrentHashMap<Field, P4jGeneral>();

	/** 类中所有标注P4jNoCopy注解的字段 */
	private Map<Field, P4jNoCopy> noCopys = new ConcurrentHashMap<Field, P4jNoCopy>();

	private Map<Field, P4jAES> aess = new ConcurrentHashMap<Field, P4jAES>();

	/** formbean类 */
	private Class clazz;

	public Class getClazz() {
		return clazz;
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	/**
	 * 添加标注aes加密字段
	 * 
	 * @param field
	 * @param aes
	 */
	public void addAes(Field field, P4jAES aes) {
		if (aes != null)
			this.aess.put(field, aes);
	}

	/**
	 * 添加标注不拷贝字段
	 * 
	 * @param field
	 * @param like
	 */
	public void addNoCopy(Field field, P4jNoCopy noCopy) {
		if (noCopy != null)
			this.noCopys.put(field, noCopy);
	}

	/**
	 * 添加标注Like字段
	 * 
	 * @param field
	 * @param like
	 */
	public void addLike(Field field, P4jLike like) {
		if (like != null)
			this.likes.put(field, like);
	}

	/**
	 * 添加标注General字段
	 * 
	 * @param field
	 * @param general
	 */
	public void addGeneral(Field field, P4jGeneral general) {
		if (general != null)
			this.generals.put(field, general);
	}

	public P4jQueryForm getQueryForm() {
		return queryForm;
	}

	public void setQueryForm(P4jQueryForm queryForm) {
		this.queryForm = queryForm;
	}

	public Map<Field, P4jLike> getLikes() {
		return likes;
	}

	public void setLikes(Map<Field, P4jLike> likes) {
		this.likes = likes;
	}

	public Map<Field, P4jGeneral> getGenerals() {
		return generals;
	}

	public void setGenerals(Map<Field, P4jGeneral> generals) {
		this.generals = generals;
	}

	public Map<Field, P4jNoCopy> getNoCopys() {
		return noCopys;
	}

	public void setNoCopys(Map<Field, P4jNoCopy> noCopys) {
		this.noCopys = noCopys;
	}

	public Map<Field, P4jAES> getAess() {
		return aess;
	}

	public void setAess(Map<Field, P4jAES> aess) {
		this.aess = aess;
	}

	@Override
	public String toString() {
		return "AnnotationFormBean [queryForm=" + queryForm + ", likes=" + likes + ", generals=" + generals
				+ ", noCopys=" + noCopys + ", aess=" + aess + ", clazz=" + clazz + "]";
	}

}
