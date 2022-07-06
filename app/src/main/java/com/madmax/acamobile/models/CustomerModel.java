package com.madmax.acamobile.models;

public class CustomerModel {
    String name;
    String phone;
    String address;
    boolean agent;

    public CustomerModel(String name, String phone, String address, boolean agent) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.agent = agent;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public boolean isAgent() {
        return agent;
    }
}
