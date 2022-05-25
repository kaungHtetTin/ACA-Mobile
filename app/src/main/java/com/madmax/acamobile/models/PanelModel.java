package com.madmax.acamobile.models;

public class PanelModel {
    String title;
    String src;
    int id;

    public PanelModel(String title, String src,int id) {
        this.title = title;
        this.src = src;
        this.id=id;
    }

    public String getTitle() {
        return title;
    }

    public String getSrc() {
        return src;
    }

    public int getId() {
        return id;
    }
}
