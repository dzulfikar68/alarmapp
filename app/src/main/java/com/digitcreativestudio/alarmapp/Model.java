package com.digitcreativestudio.alarmapp;

import java.io.Serializable;

public class Model implements Serializable {
    private int key;
    private String value;

    public Model(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
