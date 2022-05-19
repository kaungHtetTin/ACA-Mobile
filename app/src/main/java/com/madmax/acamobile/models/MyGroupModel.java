package com.madmax.acamobile.models;

public class MyGroupModel {
    String group_id;
    String name,description;
    String imageUrl;

    public MyGroupModel(String group_id, String name, String description, String imageUrl) {
        this.group_id = group_id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description=description;
    }

    public String getGroup_id() {
        return group_id;
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
