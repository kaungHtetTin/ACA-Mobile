package com.madmax.acamobile.models;

public class PanelModel {
    String title;
    int src;
    int id;

    public PanelModel(String title, int src,int id) {
        this.title = title;
        this.src = src;
        this.id=id;
    }

    public String getTitle() {
        return title;
    }

    public int getSrc() {
        return src;
    }

    public int getId() {
        return id;
    }
}
