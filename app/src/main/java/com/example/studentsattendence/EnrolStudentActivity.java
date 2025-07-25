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

public class EnrolStudentActivity extends AppCompatActivity {

    private CustomAdapter adapter;
    private ArrayList<Student> studentList = new ArrayList<>();
    private ListView listView;
    private Spinner spinner;
    private ArrayList<String> gradeList = new ArrayList<>();
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_student);

        listView = findViewById(R.id.listView);
        spinner = findViewById(R.id.spinner_grade);

        database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/")
                .getReference();

        adapter = new CustomAdapter(this, studentList);
        listView.setAdapter(adapter);
        Button btnAdd = findViewById(R.id.btnRemove);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(v); // call your existing method
            }
        });

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

                ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(EnrolStudentActivity.this,
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

    private void loadStudentsByGrade(String grade) {
        database.child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                Log.d("FirebaseDebug", "Snapshot children count: " + snapshot.getChildrenCount());
                for (DataSnapshot child : snapshot.getChildren()) {
                    if (Student.isEligible(child, grade)) {
                        studentList.add(Student.fromSnapshot(child));
                    }
                }
                System.out.println("Students found for grade: " + grade + ", Count: " + studentList.size());

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseDebug", "loadStudents:onCancelled", error.toException());
            }
        });
    }


    public void add(View view) {
        ArrayList<String> selectedIDs = adapter.getCheckedIDs();

        for (String id : selectedIDs) {
            database.child("students").child(id).child("assigned").setValue(true);
            database.child("students").child(id).child("approved").setValue(false);
        }
        String selectedGrade = spinner.getSelectedItem().toString();
        loadStudentsByGrade(selectedGrade);
    }
}