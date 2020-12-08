package com.beenet.beenetplay_tv.model;

public class LoginResponse {

    /**
     * Clase que modela el objeto JSON  de Login que proviene de la API
     * */

    private int status_code;
    private int error_code;
    private int timestamp;
    private String error_description;
    private String extra_data;

    public LoginResponse(int status_code, int error_code, int timestamp, String error_description, String extra_data) {
        this.status_code = status_code;
        this.error_code = error_code;
        this.timestamp = timestamp;
        this.error_description = error_description;
        this.extra_data = extra_data;
    }

    public int getStatus_code() {
        return status_code;
    }

    public int getError_code() {
        return error_code;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getError_description() {
        return error_description;
    }

    public String getExtra_data() {
        return extra_data;
    }

}
