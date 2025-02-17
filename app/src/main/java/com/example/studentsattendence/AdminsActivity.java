package com.example.studentsattendence;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // If no user is logged in, redirect to LoginActivity
        if (firebaseUser == null) {
            startActivity(new Intent(AdminsActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Find Welcome TextView (Ensure ID Matches XML)
        textView = findViewById(R.id.textView);

        // Firebase Database Reference
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/");
        myRef = database.getReference("users").child(firebaseUser.getUid());

        // Fetch and Display Admin Name
        fetchUserName();
    }

    private void fetchUserName() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        textView.setText("Welcome! " + name);
                    } else {
                        textView.setText("Welcome! Admin");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase Error", databaseError.getMessage());
            }
        });
    }
}
