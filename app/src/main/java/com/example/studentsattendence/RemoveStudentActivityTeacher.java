package com.example.studentsattendence;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class RemoveStudentActivityTeacher extends BaseRemoveStudentActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_remove_student;
    }

    @Override
    protected void loadStudentsByGrade(String grade) {
        database.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String stdGrade = child.child("grade").getValue(String.class);
                    Boolean assigned = child.child("assigned").getValue(Boolean.class);
                    Boolean approved = child.child("approved").getValue(Boolean.class);
                    Boolean current = child.child("currentStudent").getValue(Boolean.class);
                    String name = child.child("name").getValue(String.class);
                    String id = child.child("studentID").getValue(String.class);

                    if (stdGrade != null && stdGrade.equals(grade)
                            && Boolean.TRUE.equals(assigned)
                            && Boolean.TRUE.equals(approved)
                            && Boolean.TRUE.equals(current)) {
                        studentList.add(new Student(name, false, id));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseDebug", "loadStudents:onCancelled", error.toException());
            }
        });
    }

    @Override
    protected void removeStudents() {
        for (String id : adapter.getCheckedIDs()) {
            database.child("students").child(id).child("assigned").setValue(false);
        }
        loadStudentsByGrade(spinner.getSelectedItem().toString());
    }
}
