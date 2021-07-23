package com.touchizen.idolface.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Idol implements Serializable {

	public static final String ID = "id";

	public static final String CREATEDATE = "createDate";
	public static final String IMAGEURL = "imageUrl";
	public static final String NAME = "name";
	public static final String BIRTH = "birth";
	public static final String BODYPROFILE = "bodyProfile";
	public static final String COMPANY = "company";
	public static final String AVGRATE = "avgRate";
	public static final String PICTURECOUNT = "pictureCount";
	public static final String AWARDS = "awards";
	public static final String GROUPNAME = "groupName";
	public static final String JOBCLASS = "jobClass";

	private String id;
	private long createDate;
	private String imageUrl;
	private String name;
	private String birth;
	private String bodyProfile;
	private String company;
	private float avgRate;
	private long pictureCount;
	private String awards;
	private String groupName;
	private String jobClass;

	public Idol() {
		createDate = new Date().getTime();
	}

	public Map<String, Object> toMap() {
		HashMap<String, Object> result = new HashMap<>();

		result.put(ID, id);
		result.put(CREATEDATE, createDate);
		result.put(IMAGEURL, imageUrl);
		result.put(NAME, name);
		result.put(BIRTH, birth);
		result.put(BODYPROFILE, bodyProfile);
		result.put(COMPANY, company);
		result.put(AVGRATE, avgRate);
		result.put(PICTURECOUNT, pictureCount);
		result.put(AWARDS, awards);
		result.put(GROUPNAME, groupName);
		result.put(JOBCLASS, jobClass);

		return result;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBirth() {
		return birth;
	}

	public void setBirth(String birth) {
		this.birth = birth;
	}

	public String getBodyProfile() {
		return bodyProfile;
	}

	public void setBodyProfile(String bodyProfile) {
		this.bodyProfile = bodyProfile;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public float getAvgRate() {
		return avgRate;
	}

	public void setAvgRate(float avgRate) {
		this.avgRate = avgRate;
	}

	public long getPictureCount() {
		return pictureCount;
	}

	public void setPictureCount(long pictureCount) {
		this.pictureCount = pictureCount;
	}

	public String getAwards() {
		return awards;
	}

	public void setAwards(String awards) {
		this.awards = awards;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}
}
