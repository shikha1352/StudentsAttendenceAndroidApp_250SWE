package com.example.studentsattendence;

import com.google.firebase.database.DataSnapshot;

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


    public static Student fromSnapshot(DataSnapshot snapshot) {
        String name = snapshot.child("name").getValue(String.class);
        String id = snapshot.child("studentID").getValue(String.class);
        return new Student(name, false, id);
    }


    public static boolean isEligible(DataSnapshot snapshot, String gradeFilter) {
        String grade = snapshot.child("grade").getValue(String.class);
        Boolean assigned = snapshot.child("assigned").getValue(Boolean.class);
        Boolean currentStudent = snapshot.child("currentStudent").getValue(Boolean.class);

        return grade != null &&
                grade.equals(gradeFilter) &&
                Boolean.TRUE.equals(currentStudent) &&
                (assigned == null || !assigned);
    }
}
