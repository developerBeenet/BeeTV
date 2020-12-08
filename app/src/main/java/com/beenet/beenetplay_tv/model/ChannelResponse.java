package com.beenet.beenetplay_tv.model;

import java.util.List;

public class ChannelResponse {

    /**
     * Clase que modela (ChannelResponse + Channel) el objeto JSON de Canales que proviene de API
     * */
    private int status_code;
    private int error_code;
    private int timestamp;
    private String error_description;
    private String extra_data;
    private List<Channel> response_object;

    public ChannelResponse(int status_code, int error_code, int timestamp, String error_description, String extra_data, List<Channel> response_object) {
        this.status_code = status_code;
        this.error_code = error_code;
        this.timestamp = timestamp;
        this.error_description = error_description;
        this.extra_data = extra_data;
        this.response_object = response_object;
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

    public List<Channel> getResponse_object() {
        return response_object;
    }

}
