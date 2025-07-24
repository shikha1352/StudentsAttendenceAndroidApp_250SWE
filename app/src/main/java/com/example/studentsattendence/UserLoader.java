package com.example.studentsattendence;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;

public class UserLoader {
    private final FirebaseDatabase database;

    public UserLoader(FirebaseDatabase database) {
        this.database = database;
    }

    public interface UserLoadCallback {
        void onUserMapLoaded(HashMap<String, String> userMap);
    }

    public void loadUsers(Context context, Spinner spinner, UserLoadCallback callback) {
        DatabaseReference usersRef = database.getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, String> userList = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.child("email").getValue(String.class);
                    String userID = snapshot.getKey();
                    if (email != null && userID != null) {
                        userList.put(email, userID);
                    }
                }

                ArrayList<String> emailList = new ArrayList<>(userList.keySet());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_item, emailList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                callback.onUserMapLoaded(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("UserLoader", "Failed to load users", error.toException());
            }
        });
    }
}
