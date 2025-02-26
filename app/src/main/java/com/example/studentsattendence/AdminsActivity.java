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

public class AdminsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference myRef;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admins);

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
    }
}
