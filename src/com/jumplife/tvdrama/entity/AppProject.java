package com.jumplife.tvdrama.entity;

public class AppProject {
	private String name;
	private String title;
	private String description;
	private String iconUrl;
	private String pack;
	private String clas;
	
	public AppProject() {
		this("", "", "", "", "", "");
	}
	
	public AppProject(String name, String title, String description, String iconUrl, String pack, String clas) {
		this.name = name;
		this.title = title;
		this.description = description;
		this.iconUrl = iconUrl;
		this.pack = pack;
		this.clas = clas;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public String getPack() {
		return pack;
	}
	public void setPack(String pack) {
		this.pack = pack;
	}
	public String getClas() {
		return clas;
	}
	public void setClas(String clas) {
		this.clas = clas;
	}
}
