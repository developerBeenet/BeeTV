package com.beenet.beenetplay_tv.model;

public class Channel {

    /**
     * Clase que modela el objeto JSON que trae los canales de la API
     * */
    private String id;
    private String genre_id;
    private String title;
    private String icon_url;
    private String stream_url;

    public Channel(String id, String genre_id, String title, String icon_url, String stream_url) {
        this.id = id;
        this.genre_id = genre_id;
        this.title = title;
        this.icon_url = icon_url;
        this.stream_url = stream_url;
    }

    public String getId() {
        return id;
    }

    public String getGenre_id() {
        return genre_id;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public String getStream_url() {
        return stream_url;
    }

}
