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

public class RemoveApproveStudentActivityAdmin extends AppCompatActivity {

    private CustomAdapter adapter;
    private ArrayList<Student> studentList = new ArrayList<>();
    private ListView listView;
    private Spinner spinner;
    private ArrayList<String> gradeList = new ArrayList<>();
    private DatabaseReference database;
    private Button btnRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_student);

        listView = findViewById(R.id.listView);
        spinner = findViewById(R.id.spinner_grade);

        database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/")
                .getReference();

        adapter = new CustomAdapter(this, studentList);
        listView.setAdapter(adapter);

        btnRemove = findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeStudents(v);
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

                ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(RemoveApproveStudentActivityAdmin.this,
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
                for (DataSnapshot child : snapshot.getChildren()) {
                    String stdGrade = child.child("grade").getValue(String.class);
                    Boolean currentStudent = child.child("currentStudent").getValue(Boolean.class);
                    Boolean assigned = child.child("assigned").getValue(Boolean.class);
                    String name = child.child("name").getValue(String.class);
                    String id = child.child("studentID").getValue(String.class);

                    if(currentStudent && assigned != null && assigned == true && grade.equals(spinner.getSelectedItem().toString())){
                        studentList.add(new Student(name,assigned,id));
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

    public void removeStudents(View view) {
        ArrayList<String> selectedIDs = adapter.getCheckedIDs();
        String selectedGrade = spinner.getSelectedItem().toString();

        for (String id : selectedIDs) {
            database.child("students").child(id).child("currentStudent").setValue(false);
            database.child("students").child(id).child("assigned").setValue(false);
            database.child("students").child(id).child("approved").setValue(true);
        }

        loadStudentsByGrade(selectedGrade);
    }
}
