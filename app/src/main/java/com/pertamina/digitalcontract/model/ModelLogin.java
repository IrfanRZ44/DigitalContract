
package com.pertamina.digitalcontract.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModelLogin {

    @SerializedName("response")
    @Expose
    private Integer response;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("role")
    @Expose
    private String role;
    @SerializedName("imei")
    @Expose
    private String imei;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ModelLogin() {
    }

    /**
     * 
     * @param id
     * @param response
     * @param imei
     * @param name
     * @param role
     */
    public ModelLogin(Integer response, String id, String name, String role, String imei) {
        super();
        this.response = response;
        this.id = id;
        this.name = name;
        this.role = role;
        this.imei = imei;
    }

    public Integer getResponse() {
        return response;
    }

    public void setResponse(Integer response) {
        this.response = response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

}
