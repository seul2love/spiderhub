package com.spiderhub.vo;

import java.util.List;

public class PageInfo {

	private static final int PAGE_SIZE = 10;
	private static final String SORT_DIRECT = " asc";
	private List datas;
	private int pageSize;
	private int pageIndex;
	private String sortField;
	private String sortDirect;
	
	public PageInfo() {
		setPageSize(PAGE_SIZE);
		setSortDirect(SORT_DIRECT);
	}
	
	public PageInfo(List datas) {
		setPageSize(PAGE_SIZE);
		setSortDirect(SORT_DIRECT);
		this.datas = datas;
	}
	
	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	/**
	 * 分页中所存储的对象列表，使用了泛形?
	 * @return 分页中的存储对象
	 */
	public List getDatas() {
		return datas;
	}
	/**
	 * 分页中所存储的对象列表，使用了泛型?
	 * @param datas 分页中的存储对象
	 */
	public void setDatas(List datas) {
		this.datas = datas;
	}
	/**
	 * 每页显示多少条信息?
	 * @return
	 */
	public int getPageSize() {
		return pageSize;
	}
	/**
	 * 设置每页显示的信息数
	 * @param pageSize
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public String getSortField() {
		return sortField;
	}
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}
	public String getSortDirect() {
		return sortDirect;
	}
	public void setSortDirect(String sortDirect) {
		this.sortDirect = sortDirect;
	}
	
}
