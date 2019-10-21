
package com.pertamina.digitalcontract.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModelReviewer {

    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("nik")
    @Expose
    private String nik;
    @SerializedName("id_contract")
    @Expose
    private String id_contract;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ModelReviewer() {
    }

    /**
     * 
     * @param username
     * @param email
     * @param userId
     * @param name
     * @param nik
     */
    public ModelReviewer(String username, String name, String userId, String email, String nik, String id_contract) {
        super();
        this.username = username;
        this.name = name;
        this.userId = userId;
        this.email = email;
        this.nik = nik;
        this.id_contract = id_contract;
    }

    public String getId_contract() {
        return id_contract;
    }

    public void setId_contract(String id_contract) {
        this.id_contract = id_contract;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

}
