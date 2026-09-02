package org.example;

import java.io.Serializable;

public class TestUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    public TestUser(String name) { this.name = name; }
    public String getName() { return name; }
}
