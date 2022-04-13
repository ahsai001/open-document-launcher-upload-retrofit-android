package com.ahsailabs.dashboardinfo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ahmad s on 13/04/22.
 */
public class SaveEvidenceResponse{

    @SerializedName("message")
    private String message;

    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}