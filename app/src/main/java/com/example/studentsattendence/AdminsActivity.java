package com.example.studentsattendence;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

<<<<<<< HEAD
import android.content.Intent;
=======
>>>>>>> 721a72524146f7b61489dd93bafbf76638708844
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

public class AdminsActivity extends AppCompatActivity {

<<<<<<< HEAD
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference myRef;
    private TextView textView;

=======
>>>>>>> 721a72524146f7b61489dd93bafbf76638708844
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admins);

<<<<<<< HEAD
        textView = findViewById(R.id.textView);

        CardView createClass = findViewById(R.id.create_class_card);
        createClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a Toast message
                Toast.makeText(AdminsActivity.this, "Create Class button clicked!", Toast.LENGTH_SHORT).show();

                // Start a new activity (if needed, ensure it's the correct one)
                Intent intent = new Intent(AdminsActivity.this, AdminsActivity.class);
                startActivity(intent);
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(AdminsActivity.this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = firebaseUser.getUid();
        Log.d("UserID", userID);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/");
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
=======
        // Find views
        CardView createClass = findViewById(R.id.create_class_card);
        CardView removeStudent = findViewById(R.id.remove_std);
        CardView addTeachers = findViewById(R.id.addTeachers);
        CardView addStudents = findViewById(R.id.addStudents);
        CardView approveAddStudents = findViewById(R.id.approveAddStd);
        CardView approveRemoveStudents = findViewById(R.id.approveRemoveStd);

        // Set click listeners with logs & toast messages
        addTeachers.setOnClickListener(v -> {
            Log.d("AdminsActivity", "Add Teachers Card Clicked");
            Toast.makeText(AdminsActivity.this, "Add Teachers Clicked", Toast.LENGTH_SHORT).show();
        });

        createClass.setOnClickListener(v -> {
            Log.d("AdminsActivity", "Create Class Card Clicked");
            Toast.makeText(AdminsActivity.this, "Create Class Clicked", Toast.LENGTH_SHORT).show();
        });

        removeStudent.setOnClickListener(v -> {
            Log.d("AdminsActivity", "Remove Student Card Clicked");
            Toast.makeText(AdminsActivity.this, "Remove Student Clicked", Toast.LENGTH_SHORT).show();
        });

        addStudents.setOnClickListener(v -> {
            Log.d("AdminsActivity", "Add Students Card Clicked");
            Toast.makeText(AdminsActivity.this, "Add Students Clicked", Toast.LENGTH_SHORT).show();
        });

        approveAddStudents.setOnClickListener(v -> {
            Log.d("AdminsActivity", "Approve Add Students Card Clicked");
            Toast.makeText(AdminsActivity.this, "Approve Add Students Clicked", Toast.LENGTH_SHORT).show();
        });

        approveRemoveStudents.setOnClickListener(v -> {
            Log.d("AdminsActivity", "Approve Remove Students Card Clicked");
            Toast.makeText(AdminsActivity.this, "Approve Remove Students Clicked", Toast.LENGTH_SHORT).show();
        });

        // Firebase Setup
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/");

        if (firebaseUser != null) {
            String userID = firebaseUser.getUid();
            DatabaseReference myRef = database.getReference("users").child(userID);

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        TextView textView = findViewById(R.id.textView);
                        textView.setText("Welcome! " + name);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase Error", databaseError.getMessage());
                }
            });
        } else {
            Log.e("Auth Error", "User not logged in");
        }
>>>>>>> 721a72524146f7b61489dd93bafbf76638708844
    }
}
