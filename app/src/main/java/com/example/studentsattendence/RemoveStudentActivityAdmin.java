package com.example.studentsattendence;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class RemoveStudentActivityAdmin extends BaseRemoveStudentActivity {

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
                    Boolean currentStudent = child.child("currentStudent").getValue(Boolean.class);
                    String name = child.child("name").getValue(String.class);
                    String id = child.child("studentID").getValue(String.class);

                    if (Boolean.TRUE.equals(currentStudent) && grade.equals(stdGrade)) {
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
            database.child("students").child(id).child("currentStudent").setValue(false);
            database.child("students").child(id).child("assigned").setValue(false);
            database.child("students").child(id).child("approved").setValue(false);
        }
        loadStudentsByGrade(spinner.getSelectedItem().toString());
    }
}
