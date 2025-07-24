package com.example.studentsattendence;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;

public class AddStudentActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextName, editTextAddress, editTextFatherName, editTextFatherMobile, editTextFatherOccupation;
    private EditText editTextFatherEmail, editTextMotherEmail, editTextMotherName, editTextMotherMobile, editTextMotherOccupation;
    private EditText editTextGuardianName, editTextGuardianMobile, editTextGuardianOccupation, editTextGuardianAddress;
    private EditText editTextLandPhone, editTextWeight, editTextHeight;

    private Spinner spinnerEmail;
    private Button buttonAddImages, buttonSave;

    private DatabaseReference studentRef, userRef;
    private StorageReference storageReference;
    private FirebaseDatabase database;

    private Uri imageUri;
    private HashMap<String, String> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com");
        studentRef = database.getReference().child("students").push();
        userRef = database.getReference().child("users");
        storageReference = FirebaseStorage.getInstance().getReference();

        initializeViews();
        loadUserEmails();

        buttonAddImages.setOnClickListener(v -> openImageChooser());
        buttonSave.setOnClickListener(v -> saveStudent());
    }

    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextFatherName = findViewById(R.id.editTextFatherName);
        editTextFatherMobile = findViewById(R.id.editTextFatherMobile);
        editTextFatherOccupation = findViewById(R.id.editTextFatherOccupation);
        editTextFatherEmail = findViewById(R.id.editTextFatherEmail);
        editTextMotherEmail = findViewById(R.id.editTextMotherEmail);
        editTextMotherName = findViewById(R.id.editTextMotherName);
        editTextMotherMobile = findViewById(R.id.editTextMotherMobile);
        editTextMotherOccupation = findViewById(R.id.editTextMotherOccupation);
        editTextGuardianName = findViewById(R.id.editTextGuardianName);
        editTextGuardianAddress = findViewById(R.id.editTextGuardianAddress);
        editTextGuardianMobile = findViewById(R.id.editTextGuardianMobile);
        editTextGuardianOccupation = findViewById(R.id.editTextGuardianOccupation);
        editTextLandPhone = findViewById(R.id.editTextLandPhone);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextHeight = findViewById(R.id.editTextHeight);
        spinnerEmail = findViewById(R.id.spinner_email);
        buttonAddImages = findViewById(R.id.buttonAddImages);
        buttonSave = findViewById(R.id.buttonSave);
        userList = new HashMap<>();
    }

    private void loadUserEmails() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String email = snapshot.child("email").getValue(String.class);
                    String userID = snapshot.getKey();
                    if (email != null && userID != null) {
                        userList.put(email, userID);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddStudentActivity.this,
                        android.R.layout.simple_spinner_item,
                        new ArrayList<>(userList.keySet()));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEmail.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to load user emails.", error.toException());
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
        }
    }

    private void saveStudent() {
        if (!validateInput()) return;

        String userID = getUserIdFromSpinner();
        if (userID == null) {
            Toast.makeText(this, "Failed to find user ID for selected email.", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = studentRef.getKey();
        updateUserReference(userID, studentId);
        uploadFCMToken(userID);

        Map<String, Object> studentData = buildStudentDataMap(studentId, userID);

        if (imageUri != null) {
            uploadImage(studentId);
            studentData.put("imageUri", imageUri.toString());
        }

        studentRef.setValue(studentData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddStudentActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
                clearForm();
            } else {
                Toast.makeText(AddStudentActivity.this, "Failed to add student", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        if (getText(editTextName).isEmpty()) {
            editTextName.setError("Name required");
            return false;
        }
        return true;
    }



    private String getUserIdFromSpinner() {
        String email = spinnerEmail.getSelectedItem().toString();
        return userList.get(email);
    }

    private void updateUserReference(String userID, String studentId) {
        DatabaseReference ref = database.getReference().child("users").child(userID);
        ref.child("studentID").setValue(studentId);
        ref.child("name").setValue(getText(editTextGuardianName));
        ref.child("address").setValue(getText(editTextGuardianAddress));
        ref.child("email").setValue(spinnerEmail.getSelectedItem().toString());
        ref.child("mobile").setValue(getText(editTextGuardianMobile));
        ref.child("role").setValue("parent");
    }

    private void uploadFCMToken(String userID) {
        DatabaseReference ref = database.getReference().child("users").child(userID);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ref.child("token").setValue(task.getResult());
                    } else {
                        Log.e("FCMToken", "Failed to get token", task.getException());
                    }
                });
    }
    private String getText(EditText editText) {
        return editText.getText().toString().trim();
    }

    private Map<String, Object> buildStudentDataMap(String studentId, String userID) {
        ParentInfo father = new ParentInfo(
                getText(editTextFatherName),
                getText(editTextFatherMobile),
                getText(editTextFatherOccupation),
                getText(editTextFatherEmail)
        );

        ParentInfo mother = new ParentInfo(
                getText(editTextMotherName),
                getText(editTextMotherMobile),
                getText(editTextMotherOccupation),
                getText(editTextMotherEmail)
        );

        GuardianInfo guardian = new GuardianInfo(
                getText(editTextGuardianName),
                getText(editTextGuardianMobile),
                getText(editTextGuardianOccupation),
                getText(editTextGuardianAddress),
                spinnerEmail.getSelectedItem().toString(),
                userID
        );

        StudentInfo student = new StudentInfo(
                getText(editTextName),
                getText(editTextAddress),
                father,
                mother,
                guardian,
                getText(editTextLandPhone),
                getText(editTextWeight),
                getText(editTextHeight),
                studentId,
                imageUri != null ? imageUri.toString() : null
        );

        return student.toMap();
    }

    private void uploadImage(String studentId) {
        StorageReference imgRef = storageReference.child("images/" + studentId + ".jpg");
        imgRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> Log.d("ImageUpload", "Success"))
                .addOnFailureListener(e -> Log.e("ImageUpload", "Failed", e));
    }

    private void clearEditTexts(List<EditText> fields) {
        for (EditText field : fields) {
            field.setText("");
        }
    }
    private List<EditText> getStudentFields() {
        return Arrays.asList(editTextName, editTextAddress, editTextWeight, editTextHeight);
    }

    private List<EditText> getFatherFields() {
        return Arrays.asList(editTextFatherName, editTextFatherMobile, editTextFatherOccupation, editTextFatherEmail);
    }

    private List<EditText> getMotherFields() {
        return Arrays.asList(editTextMotherName, editTextMotherMobile, editTextMotherOccupation, editTextMotherEmail);
    }

    private List<EditText> getGuardianFields() {
        return Arrays.asList(editTextGuardianName, editTextGuardianMobile, editTextGuardianOccupation, editTextGuardianAddress);
    }

    private List<EditText> getOtherFields() {
        return Arrays.asList(editTextLandPhone);
    }

    private void clearForm() {
        List<EditText> allFields = new ArrayList<>();
        allFields.addAll(getStudentFields());
        allFields.addAll(getFatherFields());
        allFields.addAll(getMotherFields());
        allFields.addAll(getGuardianFields());
        allFields.addAll(getOtherFields());

        clearEditTexts(allFields);
        imageUri = null;
    }


}
