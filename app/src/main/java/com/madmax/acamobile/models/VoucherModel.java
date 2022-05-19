package com.madmax.acamobile.models;

public class VoucherModel {
    long voucher_id;
    double total_amount;
    String group_name;
    String group_image;
    String customer_name;
    boolean seen;
    boolean received;
    boolean isSoldOut;
    boolean agent;

    public VoucherModel(long voucher_id, double total_amount, String customer_name,boolean agent) {
        this.voucher_id = voucher_id;
        this.total_amount = total_amount;
        this.customer_name = customer_name;
        this.agent=agent;
    }

    public VoucherModel(long voucher_id, double total_amount, String group_name, String group_image, boolean seen, boolean received, boolean isSoldOut) {
        this.voucher_id = voucher_id;
        this.total_amount = total_amount;
        this.group_name = group_name;
        this.group_image = group_image;
        this.seen = seen;
        this.received = received;
        this.isSoldOut = isSoldOut;
    }

    public long getVoucher_id() {
        return voucher_id;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public String getGroup_name() {
        return group_name;
    }

    public String getGroup_image() {
        return group_image;
    }

    public boolean isSeen() {
        return seen;
    }

    public boolean isReceived() {
        return received;
    }

    public boolean isSoldOut() {
        return isSoldOut;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public boolean isAgent() {
        return agent;
    }
}
