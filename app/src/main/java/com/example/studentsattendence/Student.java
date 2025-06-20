package com.example.studentsattendence;

public class Student {
    private String name;
    private boolean assigned;
    private String studentID;

    public Student() {}

    public Student(String name, boolean assigned, String studentID) {
        this.name = name;
        this.assigned = assigned;
        this.studentID = studentID;
    }

    // New constructor to use in MarkAttendanceActivity
    public Student(String name, String studentID) {
        this.name = name;
        this.studentID = studentID;
        this.assigned = false;
    }

    public String getName() {
        return name;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public String getStudentID() {
        return studentID;
    }

    public boolean getAssigned() {
        return assigned;
    }
}
