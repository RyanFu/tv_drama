package com.jumplife.tvdrama.entity;

public class Chapter {
	private int id;
	private int number;
	
	public Chapter() {
		this(-1, -1);
	}
	
	public Chapter (int id, int number) {
		this.id = id;
		this.number = number;
	}
		
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	public int getNumber(){
		return number;
	}
	public void setNumber(int number){
		this.number = number;
	}
}
