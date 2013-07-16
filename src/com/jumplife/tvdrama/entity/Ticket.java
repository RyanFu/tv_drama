package com.jumplife.tvdrama.entity;

import java.io.Serializable;

/*
 * Server Data Base -> (對應) Local data structure
 * 
 * Campaign
 * imageUrl 		-> url 
 * title 			-> title
 * description 		-> description
 * 
 * Ticket
 * inverse_imageUrl -> url 
 * inverse_title 	-> title
 * precaution		-> description
 * serial_num		-> serialNum
 * 
 */

public class Ticket implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5294305972185399316L;
	
	private int id;
	private String url;
	private String title;
	private String description;
	private int serialNum;
	
	public Ticket() {
		this(-1, "", "", "", -1);
	}
	
	public Ticket (int id, String url, String title, String description, int serialNum) {
		this.id = id;
		this.url = url;
		this.title = title;
		this.description = description;
		this.serialNum = serialNum;
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
	public String getDescription(){
		return description;
	}
	public void setDescription(String description){
		this.description = description;
	}
	public int getSerialNum(){
		return serialNum;
	}
	public void setSerialNum(int serialNum){
		this.serialNum = serialNum;
	}
}
