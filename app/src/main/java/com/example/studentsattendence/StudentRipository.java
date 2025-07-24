package com.example.studentsattendence;

import com.example.studentsattendence.Student;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class StudentRipository {
    private final DatabaseReference studentRef;

    public StudentRipository(FirebaseDatabase database) {
        this.studentRef = database.getReference("students");
    }

    public void getStudentsByGrade(String grade, StudentLoadCallback callback) {
        studentRef.orderByChild("grade").equalTo(grade)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<Student> students = new ArrayList<>();
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            String name = snap.child("name").getValue(String.class);
                            String id = snap.child("studentID").getValue(String.class);
                            Boolean approved = snap.child("approved").getValue(Boolean.class);
                            Boolean assigned = snap.child("assigned").getValue(Boolean.class);
                            if (name != null && id != null && Boolean.TRUE.equals(approved) && Boolean.TRUE.equals(assigned)) {
                                students.add(new Student(name, id));
                            }
                        }
                        callback.onStudentsLoaded(students);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(error.toException());
                    }
                });
    }

    public void updateAttendance(String studentId, String date, boolean isPresent) {
        studentRef.orderByChild("studentID").equalTo(studentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            String key = snap.getKey();
                            studentRef.child(key).child("attendance").child(date).setValue(isPresent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    public interface StudentLoadCallback {
        void onStudentsLoaded(ArrayList<Student> students);
        void onError(Exception e);
    }
}
