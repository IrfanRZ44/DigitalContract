package com.pertamina.digitalcontract.model;

public class ModelLogRejected {
    String user, dateTime, message;

    public ModelLogRejected() {
    }

    public ModelLogRejected(String user, String dateTime, String message) {
        this.user = user;
        this.dateTime = dateTime;
        this.message = message;
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
