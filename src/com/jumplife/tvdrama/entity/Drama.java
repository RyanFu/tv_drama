package com.jumplife.tvdrama.entity;

public class Drama{
	private int id;
	private String chineseName;
	private String posterUrl;
	private String introduction;
	private int areaId;
	private String eps;
	private String releaseDate;
	private boolean isShow;
	private int views;
	
	public Drama() {
		this(-1, "", "", "", 1, "", false, 0, "");
	}
	
	public Drama (int id, String chineseName, String posterUrl, String introduction, int areaId, 
			String releaseDate, boolean isShow, int views, String eps) {
		this.id = id;
		this.chineseName = chineseName;
		this.posterUrl = posterUrl;
		this.introduction = introduction;
		this.areaId = areaId;
		this.releaseDate = releaseDate;
		this.isShow = isShow;
		this.views = views;
		this.eps = eps;
	}
	
	
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	public String getChineseName(){
		return chineseName;
	}
	public void setChineseName(String chineseName){
		this.chineseName = chineseName;
	}
	public String getPosterUrl(){
		return posterUrl;
	}
	public void setPosterUrl(String posterUrl){
		this.posterUrl = posterUrl;
	}
	public String getIntroduction(){
		return introduction;
	}
	public void setIntroduction(String introduction){
		this.introduction = introduction;
	}
	public int getAreId(){
		return areaId;
	}
	public void setAreId(int areaId){
		this.areaId = areaId;
	}	
	public String getReleaseDate(){
		return this.releaseDate;
	}
	public void setReleaseDate(String releaseDate){
		this.releaseDate = releaseDate;
	}
	public boolean getIsShow(){
		return this.isShow;
	}
	public void setIsShow(boolean isShow){
		this.isShow = isShow;
	}
	public int getViews(){
		return views;
	}
	public void setViews(int views){
		this.views = views;
	}
	public String getEps(){
		return eps;
	}
	public void setEps(String eps){
		this.eps = eps;
	}
}
