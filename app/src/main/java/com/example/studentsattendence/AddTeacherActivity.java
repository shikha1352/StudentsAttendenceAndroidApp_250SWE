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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddTeacherActivity extends AppCompatActivity {

    private EditText editTextName, editTextAddress, editTextMobile;
    private Button buttonSave;
    private Spinner spinnerGrade, spinnerEmail;

    private FirebaseDatabase database;
    private HashMap<String, String> userList;

    private GradeLoader gradeLoader;
    private UserLoader userLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);

        database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com");
        userList = new HashMap<>();


        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextMobile = findViewById(R.id.editTextMobile);
        buttonSave = findViewById(R.id.buttonSave);
        spinnerGrade = findViewById(R.id.spinner_grade);
        spinnerEmail = findViewById(R.id.spinner_email);


        gradeLoader = new GradeLoader(database);
        userLoader = new UserLoader(database);

        gradeLoader.loadGrades(this, spinnerGrade);
        userLoader.loadUsers(this, spinnerEmail, new UserLoader.UserLoadCallback() {
            @Override
            public void onUserMapLoaded(HashMap<String, String> userMap) {
                userList = userMap;
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTeacher();
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
                        Log.d(TAG, "Teacher added successfully!");
                        Toast.makeText(AddTeacherActivity.this, "Teacher added successfully", Toast.LENGTH_SHORT).show();
                        clearForm();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error updating fields: " + e.getMessage());
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
