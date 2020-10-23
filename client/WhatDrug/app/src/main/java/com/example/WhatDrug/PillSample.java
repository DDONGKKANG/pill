package com.example.WhatDrug;

public class PillSample {
    private String number;
    private String name;
    private String img_uri;
    private String pill_front;
    private String pill_back;
    private String shape;
    private String color;
    private String classfication;
    private String effect;
    private String take;
    private String store;
    private String warning;

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getTake() {
        return take;
    }

    public void setTake(String take) {
        this.take = take;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    @Override
    public String toString() {
        return "PillSample{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", img_uri='" + img_uri + '\'' +
                ", pill_front='" + pill_front + '\'' +
                ", pill_back='" + pill_back + '\'' +
                ", shape='" + shape + '\'' +
                ", color='" + color + '\'' +
                ", classfication='" + classfication + '\'' +
                ", effect='" + effect + '\'' +
                ", take='" + take + '\'' +
                ", store='" + store + '\'' +
                ", warning='" + warning + '\'' +
                '}';
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg_uri() {
        return img_uri;
    }

    public void setImg_uri(String img_uri) {
        this.img_uri = img_uri;
    }

    public String getPill_front() {
        return pill_front;
    }

    public void setPill_front(String pill_front) {
        this.pill_front = pill_front;
    }

    public String getPill_back() {
        return pill_back;
    }

    public void setPill_back(String pill_back) {
        this.pill_back = pill_back;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getClassfication() {
        return classfication;
    }

    public void setClassfication(String classfication) {
        this.classfication = classfication;
    }

}
