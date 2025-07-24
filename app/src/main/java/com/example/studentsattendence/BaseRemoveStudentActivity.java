package com.example.studentsattendence;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public abstract class BaseRemoveStudentActivity extends AppCompatActivity {

    protected CustomAdapter adapter;
    protected ArrayList<Student> studentList = new ArrayList<>();
    protected ListView listView;
    protected Spinner spinner;
    protected ArrayList<String> gradeList = new ArrayList<>();
    protected DatabaseReference database;
    protected Button btnRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        listView = findViewById(R.id.listView);
        spinner = findViewById(R.id.spinner_grade);
        btnRemove = findViewById(R.id.btnRemove);

        database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/")
                .getReference();

        adapter = new CustomAdapter(this, studentList);
        listView.setAdapter(adapter);

        btnRemove.setOnClickListener(v -> removeStudents());

        loadGrades();
        setupSpinnerListener();
    }

    private void loadGrades() {
        database.child("Classes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gradeList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String grade = snap.child("grade").getValue(String.class);
                    if (grade != null) {
                        gradeList.add(grade);
                    }
                }

                ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(BaseRemoveStudentActivity.this,
                        android.R.layout.simple_spinner_item, gradeList);
                gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(gradeAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "loadGrades:onCancelled", error.toException());
            }
        });
    }

    private void setupSpinnerListener() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGrade = gradeList.get(position);
                loadStudentsByGrade(selectedGrade);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Abstract methods for subclass-specific behavior
    protected abstract int getLayoutId();
    protected abstract void loadStudentsByGrade(String grade);
    protected abstract void removeStudents();
}
