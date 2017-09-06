package com.partner4java.p4jorm.bean.page;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回数据<br/>
 * 
 * @author partner4java
 * 
 * @param <T>
 *            返回数据类型
 */
public class PageData<T> implements Serializable {
	private static final long serialVersionUID = -1988099029056673775L;

	/** 返回数据列表 */
	private List<T> resultlist;

	/** 总数量 */
	private long totalCount;

	/** 分页相关数据 */
	private PageIndex pageIndex;

	public List<T> getResultlist() {
		return resultlist;
	}

	public void setResultlist(List<T> resultlist) {
		this.resultlist = resultlist;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
		this.pageIndex.setEndPage(new Long((totalCount + this.pageIndex.getMaxResult() - 1) / this.pageIndex.getMaxResult()).intValue());
	}

	public PageIndex getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(PageIndex pageIndex) {
		this.pageIndex = pageIndex;
	}

	@Override
	public String toString() {
		return "PageData [resultlist=" + resultlist + ", totalCount=" + totalCount + ", pageIndex=" + pageIndex + "]";
	}

}
