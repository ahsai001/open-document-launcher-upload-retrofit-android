package com.ahsailabs.dashboardinfo;

import com.google.gson.annotations.SerializedName;

public class SaveWLResponse{
	@SerializedName("message")
	private String message;
	public String getMessage(){
		return message;
	}
}