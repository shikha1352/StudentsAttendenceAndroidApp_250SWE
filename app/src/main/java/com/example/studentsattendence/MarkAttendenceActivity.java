package com.example.studentsattendence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MarkAttendenceActivity extends AppCompatActivity {
    private ListView listView;
    private CustomAdapter adapter;
    private ArrayList<Student> studentList = new ArrayList<>();
    private ArrayList<String> presentIDs;
    private FirebaseDatabase db;
    private String teacherGrade, teacherName, userId, currentDate;

    private StudentRipository studentRepo;
    private AttendanceReportGenerator reportGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendence);

        db = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/");
        studentRepo = new StudentRipository(db);


        listView = findViewById(R.id.listView);
        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            db.getReference("users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        public void onDataChange(DataSnapshot snapshot) {
                            teacherName = snapshot.child("name").getValue(String.class);
                            teacherGrade = snapshot.child("grade").getValue(String.class);
                            loadStudents();
                        }
                        public void onCancelled(DatabaseError error) {}
                    });
        }
        reportGenerator = new AttendanceReportGenerator(this, db);
        findViewById(R.id.btn_generate_pdf).setOnClickListener(v -> {
            reportGenerator.generate(teacherGrade);
        });

        listView.setOnItemClickListener((parent, view, pos, id) -> {
            Student s = studentList.get(pos);
            s.setAssigned(!s.getAssigned());
            adapter.notifyDataSetChanged();
        });
    }

    private void loadStudents() {
        studentRepo.getStudentsByGrade(teacherGrade, new StudentRipository.StudentLoadCallback() {
            public void onStudentsLoaded(ArrayList<Student> students) {
                studentList.clear();
                studentList.addAll(students);
                adapter = new CustomAdapter(MarkAttendenceActivity.this, studentList);
                listView.setAdapter(adapter);
            }
            public void onError(Exception e) {
                Toast.makeText(MarkAttendenceActivity.this, "Error loading students", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void submit(View v) {
        presentIDs = adapter.getCheckedIDs();
        for (Student s : studentList) {
            boolean present = presentIDs.contains(s.getId());
            studentRepo.updateAttendance(s.getId(), currentDate, present);
        }
    }
}

