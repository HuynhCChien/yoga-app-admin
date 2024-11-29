package com.example.yoga_adminv3.model;

public class ClassInstance {
    private int id;
    private int courseId;  // Foreign key linking to Course
    private String date;
    private String teacher;
    private String comments;

    public ClassInstance() {

    }

    // Constructor, Getters, and Setters
    public ClassInstance(int id, int courseId, String date, String teacher, String comments) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
    }

    // Getters and Setters for each field

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
