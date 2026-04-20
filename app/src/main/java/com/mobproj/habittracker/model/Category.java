package com.mobproj.habittracker.model;

public class Category {
    public static final String TYPE_HABIT = "HABIT";
    public static final String TYPE_WORKOUT = "WORKOUT";

    private long id;
    private long userId;
    private String name;
    private String type;
    private String color;

    public Category() {}

    public Category(long id, long userId, String name, String type, String color) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.color = color;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public String toString() {
        return name;
    }
}
