package com.mobproj.habittracker.model;

public class Workout {
    public static final String INTENSITY_LOW = "LOW";
    public static final String INTENSITY_MEDIUM = "MEDIUM";
    public static final String INTENSITY_HIGH = "HIGH";

    private long id;
    private long userId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private int durationMinutes;
    private String intensity;
    private long createdAt;

    public Workout() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getIntensity() { return intensity; }
    public void setIntensity(String intensity) { this.intensity = intensity; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
