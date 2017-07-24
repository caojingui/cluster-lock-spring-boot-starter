package com.tianba.starter.clusterlock.domain;

import java.util.List;

public class User {

    private String name;
    private int age;
    private List<String> loves;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getLoves() {
        return loves;
    }

    public void setLoves(List<String> loves) {
        this.loves = loves;
    }
}
