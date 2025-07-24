package com.example.studentsattendence;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class GradeLoader {
    private final FirebaseDatabase database;

    public GradeLoader(FirebaseDatabase database) {
        this.database = database;
    }

    public void loadGrades(Context context, Spinner spinner) {
        DatabaseReference gradeRef = database.getReference("Classes");

        gradeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> gradeList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String grade = snapshot.child("grade").getValue(String.class);
                    if (grade != null) {
                        gradeList.add(grade);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_item, gradeList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("GradeLoader", "Failed to load grades", error.toException());
            }
        });
    }
}
