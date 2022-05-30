package com.madmax.acamobile.models;

public class RankModel {
    int rank_id;
    String rank;

    public RankModel(int rank_id, String rank) {
        this.rank_id = rank_id;
        this.rank = rank;
    }

    public int getRank_id() {
        return rank_id;
    }

    public String getRank() {
        return rank;
    }
}
