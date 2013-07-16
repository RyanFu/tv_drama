package com.jumplife.tvdrama.entity;

public class Section {
	private int id;
	private String url;
	private String title;
	
	public Section() {
		this(-1, "", "");
	}
	
	public Section (int id, String url, String title) {
		this.id = id;
		this.url = url;
		this.title = title;
	}
		
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	public String getUrl(){
		return url;
	}
	public void setUrl(String url){
		this.url = url;
	}
	public String getTitle(){
		return title;
	}
	public void setTitle(String title){
		this.title = title;
	}
}
