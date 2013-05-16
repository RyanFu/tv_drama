package com.jumplife.tvdrama.entity;

public class AppProject {
	private String name;
	private String iconUrl;
	private String pack;
	private String clas;
	
	public AppProject() {
		this("", "", "", "");
	}
	
	public AppProject(String name, String iconUrl, String pack, String clas) {
		this.name = name;
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
