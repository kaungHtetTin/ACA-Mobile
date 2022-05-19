package com.madmax.acamobile.models;

public class TargetPlanModel {
    int planId;
    long startDate;
    long endDate;

    public TargetPlanModel(int planId, long startDate, long endDate) {
        this.planId = planId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getPlanId() {
        return planId;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }
}
