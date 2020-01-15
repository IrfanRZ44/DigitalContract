package com.pertamina.digitalcontract.model;

public class ModelLogRejected {
    String dateTime, user, message, status;

    public ModelLogRejected() {
    }

    public ModelLogRejected(String dateTime, String user, String status, String message) {
        this.dateTime = dateTime;
        this.user = user;
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
