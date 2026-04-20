package com.mobproj.habittracker.model;

public class ProgressRecord {
    public static final String TYPE_HABIT = "HABIT";
    public static final String TYPE_WORKOUT = "WORKOUT";

    private long id;
    private long userId;
    private long itemId;
    private String itemType;
    private String itemName;
    private String date; // ISO yyyy-MM-dd
    private boolean completed;
    private String notes;

    public ProgressRecord() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getItemId() { return itemId; }
    public void setItemId(long itemId) { this.itemId = itemId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
