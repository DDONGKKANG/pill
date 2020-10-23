package com.example.WhatDrug;

public class SingerItem {

    private String name_pill;
    private String week_pill;
    private String time_pill;
    private int img_pill;

    public SingerItem(String name_pill, String week_pill, String time_pill, int img_pill) {
        this.name_pill = name_pill;
        this.week_pill = week_pill;
        this.time_pill = time_pill;
        this.img_pill = img_pill;
    }

    public String getName() { return name_pill; }

    public void setName(String name_pill) { this.name_pill = name_pill; }

    public String getWeek() { return week_pill; }

    public void setWeek(String mobile) { this.week_pill = week_pill; }

    public String getTime() { return time_pill; }

    public void setTime(String time_pill) { this.time_pill = time_pill; }

    public int getImg() { return img_pill; }

    public void setImg(int img_pill) { this.img_pill = img_pill; }

}
