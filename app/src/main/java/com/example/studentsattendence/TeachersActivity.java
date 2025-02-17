package com.example.studentsattendence;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TeachersActivity extends AppCompatActivity {

    public void createCLass(View view){

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase database  = FirebaseDatabase.getInstance("https://attendme-644ac-default-rtdb.asia-southeast1.firebasedatabase.app/");
        String userID = null;
        if (firebaseUser != null) {
            userID = firebaseUser.getUid();
            System.out.println(userID);
        }
        DatabaseReference myRef = database.getReference("users").child(userID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    TextView textView = findViewById(R.id.textView);
                    textView.setText("Welcome! " + name );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase Error", databaseError.getMessage());
            }
        });
    }
}