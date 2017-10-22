package com.example.wickedpuppets.beabuy;

import java.io.Serializable;

/**
 * Created by pgurdek on 22.10.17.
 */

public class User implements Serializable{

    private String name = "";

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
