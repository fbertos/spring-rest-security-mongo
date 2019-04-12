package org.fbertos.persistence.search;

public class Filter {
	private String query;
	private String order;
	private String page;
	private String itemsperpage;
	
	public Filter() {
		super();
	}
	
	public Filter(String query, String order, String page, String itemsperpage) {
		super();
		this.query = query;
		this.order = order;
		this.page = page;
		this.itemsperpage = itemsperpage;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getItemsperpage() {
		return itemsperpage;
	}

	public void setItemsperpage(String itemsperpage) {
		this.itemsperpage = itemsperpage;
	}
}
