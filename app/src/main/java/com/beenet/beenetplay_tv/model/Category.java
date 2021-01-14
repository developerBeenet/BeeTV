package com.beenet.beenetplay_tv.model;

public class Category {

    /**
     * Clase que modela el objeto JSON que trae las Categorias de la API
     * */

    private int id;
    private String name;
    private String icon;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(int id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return name;
    }

}
