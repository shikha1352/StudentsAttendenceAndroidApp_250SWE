package com.example.studentsattendence;

import static android.content.ContentValues.TAG;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddApproveStudentActivityAdmin extends AppCompatActivity {

    private CustomAdapter adapter;
    private ArrayList<Student> studentToDisplay;
    private ArrayList<String> gradeList;
    private ListView listView;
    private Spinner spinner;

    private DatabaseReference studentsRef;
    private DatabaseReference gradesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_add_student);

        // Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/");
        studentsRef = database.getReference("students");
        gradesRef = database.getReference("Classes");

        // Initialize views and lists
        spinner = findViewById(R.id.spinner_grade);
        listView = findViewById(R.id.listView);
        studentToDisplay = new ArrayList<>();
        gradeList = new ArrayList<>();

        adapter = new CustomAdapter(this, studentToDisplay);
        listView.setAdapter(adapter);

        // Populate grade spinner
        gradesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gradeList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String grade = snap.child("grade").getValue(String.class);
                    if (grade != null) {
                        gradeList.add(grade);
                    }
                }
                ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(AddApproveStudentActivityAdmin.this, android.R.layout.simple_spinner_item, gradeList);
                gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(gradeAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load grades: " + error.getMessage());
            }
        });

        Button btnremove = findViewById(R.id.btnRemove);
        btnremove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveSelectedStudents(v); // call your existing method
            }
        });
        // Handle grade selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGrade = gradeList.get(position);
                loadUnapprovedStudents(selectedGrade);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadUnapprovedStudents(String grade) {
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentToDisplay.clear();
                for (DataSnapshot studentSnap : snapshot.getChildren()) {
                    String name = studentSnap.child("name").getValue(String.class);
                    Boolean assigned = studentSnap.child("assigned").getValue(Boolean.class);
                    Boolean approved = studentSnap.child("approved").getValue(Boolean.class);
                    Boolean current = studentSnap.child("currentStudent").getValue(Boolean.class);
                    String stdID = studentSnap.child("studentID").getValue(String.class);
                    String stdGrade = studentSnap.child("grade").getValue(String.class);

                    if (name != null && stdID != null && stdGrade != null &&
                            Boolean.TRUE.equals(assigned) &&
                            Boolean.FALSE.equals(approved) &&
                            Boolean.TRUE.equals(current) &&
                            stdGrade.equals(grade)) {

                        studentToDisplay.add(new Student(name, false, stdID)); // unchecked by default
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load students: " + error.getMessage());
            }
        });
    }

    public void approveSelectedStudents(View view) {
        ArrayList<String> selectedIDs = adapter.getCheckedIDs();

        for (String stdID : selectedIDs) {
            Query query = studentsRef.orderByChild("studentID").equalTo(stdID);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot studentSnap : snapshot.getChildren()) {
                        DatabaseReference studentRef = studentSnap.getRef();
                        studentRef.child("approved").setValue(true);
                        studentRef.child("currentStudent").setValue(true);
                        studentRef.child("assigned").setValue(true);
                    }
                    // Optionally reload students after approval
                    String selectedGrade = spinner.getSelectedItem().toString();
                    loadUnapprovedStudents(selectedGrade);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to approve student: " + error.getMessage());
                }
            });
        }
    }
}
