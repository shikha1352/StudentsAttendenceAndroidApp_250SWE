package com.example.studentsattendence;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddTeacherActivity extends AppCompatActivity {

    private EditText editTextName, editTextAddress,editTextMobile,editTextEmail;
    private Button buttonSave;
    private DatabaseReference databaseReference;
    DatabaseReference userRef;
    private StorageReference storageReference;
    private ArrayList<String> gradeList;
    private HashMap<String,String> userList;
    private Spinner spinnerGrade,spinnerEmail;
    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);

        gradeList = new ArrayList<>();
        userList = new HashMap<>();

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com");

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextMobile = findViewById(R.id.editTextMobile);
        buttonSave = findViewById(R.id.buttonSave);
        spinnerGrade = findViewById(R.id.spinner_grade);
        spinnerEmail = findViewById(R.id.spinner_email);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTeacher();
            }
        });

        loadGrades();
        loadUsers();
    }

    private void loadGrades() {
        DatabaseReference gradeRef = database.getReference("Classes");

        gradeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gradeList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String grade = snapshot.child("grade").getValue(String.class);
                    if (grade != null) {
                        gradeList.add(grade);
                    }
                }

                // Update the spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTeacherActivity.this,
                        android.R.layout.simple_spinner_item, gradeList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerGrade.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read grades.", databaseError.toException());
            }
        });
    }

    private void loadUsers() {
        DatabaseReference usersRef = database.getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.child("email").getValue(String.class);
                    String userID = snapshot.getKey();  // Get Firebase-generated userID

                    if (email != null && userID != null) {
                        userList.put(email, userID);
                    }
                }

                // Update the spinner
                ArrayList<String> emailList = new ArrayList<>(userList.keySet());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTeacherActivity.this,
                        android.R.layout.simple_spinner_item, emailList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEmail.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read users.", databaseError.toException());
            }
        });
    }

    private void saveTeacher() {
        String name = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String mobile = editTextMobile.getText().toString().trim();
        String grade = spinnerGrade.getSelectedItem() != null ? spinnerGrade.getSelectedItem().toString() : "";
        String email = spinnerEmail.getSelectedItem() != null ? spinnerEmail.getSelectedItem().toString() : "";
        String userID = userList.get(email);

        if (userID == null || userID.isEmpty()) {
            Toast.makeText(this, "Failed to find user ID for selected email.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = database.getReference("users").child(userID);

        Map<String, Object> teacherData = new HashMap<>();
        teacherData.put("name", name);
        teacherData.put("address", address);
        teacherData.put("mobile", mobile);
        teacherData.put("grade", grade);
        teacherData.put("role", "teacher");

        userRef.updateChildren(teacherData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Fields updated successfully!");
                        Toast.makeText(AddTeacherActivity.this, "Teacher added successfully", Toast.LENGTH_SHORT).show();
                        clearForm();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Error updating fields: " + e.getMessage());
                        Toast.makeText(AddTeacherActivity.this, "Adding teacher failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearForm() {
        editTextName.setText("");
        editTextAddress.setText("");
        editTextMobile.setText("");
    }
}
