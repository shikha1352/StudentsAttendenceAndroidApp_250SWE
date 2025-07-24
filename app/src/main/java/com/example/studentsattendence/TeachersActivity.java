package com.example.studentsattendence;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TeachersActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference myRef;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);

        textView = findViewById(R.id.textView);
        CardView enrollStudent = findViewById(R.id.enrollStudents);
        CardView markAttendance = findViewById(R.id.markAttendance);
        CardView removeStudent = findViewById(R.id.removeStudent);

        enrollStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEnrollStudentScreen();
            }
        });

        markAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMarkAttendanceScreen();
            }
        });

        removeStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRemoveStudentScreen();
            }
        });

        initializeFirebaseAndLoadUser();
    }

    private void openEnrollStudentScreen() {
        Intent intent = new Intent(this, EnrolStudentActivity.class);
        startActivity(intent);
    }

    private void openMarkAttendanceScreen() {
        Toast.makeText(this, "Mark Attendance button clicked!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MarkAttendenceActivity.class);
        startActivity(intent);
    }

    private void openRemoveStudentScreen() {
        // Opens the student removal screen
        Intent intent = new Intent(this, RemoveStudentActivityTeacher.class);
        startActivity(intent);
    }

    private void initializeFirebaseAndLoadUser() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/");
        String userID = null;

        if (firebaseUser != null) {
            userID = firebaseUser.getUid();
            System.out.println(userID);
        }

        myRef = database.getReference("users").child(userID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    textView.setText("Welcome! " + name);
                } else {
                    textView.setText("User not found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase Error", databaseError.getMessage());
            }
        });
    }
}
