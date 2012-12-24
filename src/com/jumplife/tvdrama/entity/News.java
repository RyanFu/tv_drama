package com.jumplife.tvdrama.entity;

import java.util.Date;

public class News
{
	private String title;
	private String thumbnailUrl;
	private Date releaseDate;
	private String link;
	private String content;
	private String pictureUrl;
	private int type;
	private String source;
	
	public News() {
		new News("", "", new Date(), "", "", "", -1, "");
	}
	
	public News(String title, String thumbnailUrl, Date releaseDate, String link, String content, String pictureUrl, int type, String source) {
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
		this.releaseDate = releaseDate;
		this.link = link;
		this.content = content;
		this.pictureUrl = pictureUrl;
		this.type = type;
		this.source = source;
	}
	
	public static final int TYPE_LINK = 1;
	public static final int TYPE_PIC = 2;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public Date getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getPictureUrl() {
		return pictureUrl;
	}
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
