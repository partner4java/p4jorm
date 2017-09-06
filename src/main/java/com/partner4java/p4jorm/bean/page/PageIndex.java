package com.partner4java.p4jorm.bean.page;

import java.io.Serializable;

/**
 * 分页索引，存放分页相关运算、显示等基础数据<br/>
 * 如：分页相关当前页、分页暂时可选择开始页和结束页、可选择页数、每页显示数目等
 * 
 * @author partner4java
 * 
 */
public class PageIndex implements Serializable {
	private static final long serialVersionUID = 1917301024504525009L;

	/** 当前列表页开始页码 */
	private int startPage = 1;
	/** 当前列表页结束页码 */
	private int endPage = 10;

	/** 当前列表页最大消失可选页数 */
	private int maxPage = 10;

	/** 当前列表页的显示数目（每页显示数量） */
	private int maxResult = 20;

	/** 当前列表页当前页码 */
	private int currentPage = 1;

	public PageIndex() {
		super();
	}

	public PageIndex(int currentPage) {
		super();
		if (currentPage < 1) {
			currentPage = 1;
		}
		this.currentPage = currentPage;
	}

	public int getStartPage() {
		return startPage;
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	public int getEndPage() {
		return endPage;
	}

	public void setEndPage(int endPage) {
		this.endPage = endPage;

		// 判断是否操作了最大可选页数范围
		if (this.endPage > this.maxPage && (this.endPage - this.currentPage) > this.maxPage / 2
				&& this.currentPage >= this.maxPage / 2) {
			this.endPage = (this.currentPage + this.maxPage / 2) - 1;
			this.startPage = this.currentPage - this.maxPage / 2;
		} else if (this.endPage > this.maxPage && this.currentPage < this.maxPage / 2) {
			this.startPage = 1;
			this.endPage = this.maxPage;
		} else if (this.endPage > this.maxPage) {
			// this.endPage = this.maxPage;
			this.startPage = this.endPage - this.maxPage;
		}

		if (this.endPage < 1) {
			this.endPage = 1;
		}

		if (this.startPage < 1) {
			this.startPage = 1;
		}
	}

	public int getMaxPage() {
		return maxPage;
	}

	public void setMaxPage(int maxPage) {
		this.maxPage = maxPage;
	}

	public int getMaxResult() {
		return maxResult;
	}

	public void setMaxResult(int maxResult) {
		this.maxResult = maxResult;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	@Override
	public String toString() {
		return "PageIndex [startPage=" + startPage + ", endPage=" + endPage + ", maxPage=" + maxPage + ", maxResult="
				+ maxResult + ", currentPage=" + currentPage + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + currentPage;
		result = prime * result + endPage;
		result = prime * result + maxPage;
		result = prime * result + maxResult;
		result = prime * result + startPage;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PageIndex other = (PageIndex) obj;
		if (currentPage != other.currentPage)
			return false;
		if (endPage != other.endPage)
			return false;
		if (maxPage != other.maxPage)
			return false;
		if (maxResult != other.maxResult)
			return false;
		if (startPage != other.startPage)
			return false;
		return true;
	}

}
