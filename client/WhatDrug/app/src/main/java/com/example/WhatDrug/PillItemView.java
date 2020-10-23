package com.example.WhatDrug;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class PillItemView extends LinearLayout {
    TextView textView;
    TextView textView2;
    TextView textView3;
    ImageView imageView;

    public PillItemView(Context context) {
        super(context);
        init(context);
    }

    public PillItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.pill_item, this, true);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    void setName(String name) { textView.setText(name); }

    void setShape(String shape) { textView2.setText(shape); }

    void setColor(String color) { textView3.setText(color);}

}

class PillItem {

    private String name;
    private String img_pill;
    private String img_front;
    private String img_back;
    private String shape;
    private String color;
    private String classification;
    private String effect;
    private String take;
    private String store;
    private String warning;

    public PillItem(String name, String img_pill, String img_front, String img_back, String shape, String color, String classification, String effect, String take, String store, String warning) {
        this.name = name;
        this.img_pill = img_pill;
        this.img_front = img_front;
        this.img_back = img_back;
        this.shape = shape;
        this.color = color;
        this.classification = classification;
        this.effect = effect;
        this.take = take;
        this.store = store;
        this.warning = warning;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg_pill() {
        return img_pill;
    }

    public void setImg_pill(String img_pill) {
        this.img_pill = img_pill;
    }

    public String getImg_front() {
        return img_front;
    }

    public void setImg_front(String img_front) {
        this.img_front = img_front;
    }

    public String getImg_back() {
        return img_back;
    }

    public void setImg_back(String img_back) {
        this.img_back = img_back;
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

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

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
}

