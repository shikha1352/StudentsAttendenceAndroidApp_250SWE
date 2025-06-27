package com.example.studentsattendence;

import static android.content.ContentValues.TAG;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Typeface;




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
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MarkAttendenceActivity extends AppCompatActivity {

    private CustomAdapter adapter;
    private ArrayList<Student> studentToDisplay = new ArrayList<>();
    private ArrayList<String> allStudents = new ArrayList<>();
    private ArrayList<String> presentStudents;
    private ListView listView;
    private TextView nameLabel, gradeLabel;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String userId, teacherName, teacherGrade;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendence);

        currentDate = getCurrentDate();
        database = FirebaseDatabase.getInstance("https://students-attendence-5aff8-default-rtdb.firebaseio.com/");
        myRef = database.getReference("students");

        nameLabel = findViewById(R.id.label_teacher);
        gradeLabel = findViewById(R.id.label_grade);
        listView = findViewById(R.id.listView);

        // Get teacher info
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();

            DatabaseReference userRef = database.getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        teacherName = snapshot.child("name").getValue(String.class);
                        teacherGrade = snapshot.child("grade").getValue(String.class);
                        nameLabel.setText("Teacher : " + teacherName);
                        gradeLabel.setText("Grade : " + teacherGrade);
                        loadStudentsByGrade(); // Now fetch students
                    } else {
                        Log.e(TAG, "No user data found for ID: " + userId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching teacher data", error.toException());
                }
            });

        }

        // Handle checkbox toggle
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Student student = studentToDisplay.get(position);
            student.setAssigned(!student.getAssigned());
            adapter.notifyDataSetChanged();
        });



        findViewById(R.id.btn_generate_pdf).setOnClickListener(v -> {
            generateAttendancePdf();
        });
    }

    private void loadStudentsByGrade() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentToDisplay.clear();
                allStudents.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String name = child.child("name").getValue(String.class);
                    String id = child.child("studentID").getValue(String.class);
                    String grade = child.child("grade").getValue(String.class);
                    boolean approve = child.child("approved").getValue(Boolean.class);

                    if (grade != null && grade.equals(teacherGrade) && approve==true) {
                        studentToDisplay.add(new Student(name, id));
                        allStudents.add(id);
                    }
                }

                // Set adapter after loading data
                adapter = new CustomAdapter(MarkAttendenceActivity.this, studentToDisplay);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read student data", error.toException());
            }
        });
    }

    public void submit(View view) {
        presentStudents = adapter.getCheckedIDs();

        for (String stdID : allStudents) {
            boolean isPresent = presentStudents.contains(stdID);

            myRef.orderByChild("studentID").equalTo(stdID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String studentKey = snapshot.getKey();
                                DatabaseReference attendanceRef = myRef.child(studentKey).child("attendance").child(currentDate);
                                attendanceRef.setValue(isPresent);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error writing attendance", error.toException());
                        }
                    });
        }
    }


    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
    private void generateAttendancePdf() {
        DatabaseReference studentsRef = database.getReference("students");

        studentsRef.orderByChild("grade").equalTo(teacherGrade)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        PdfDocument document = new PdfDocument();
                        Paint paint = new Paint();
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                        PdfDocument.Page page = document.startPage(pageInfo);
                        Canvas canvas = page.getCanvas();

                        int y = 50;
                        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                        paint.setTextSize(18);
                        canvas.drawText("Student Attendance Report", 150, y, paint);
                        y += 40;

                        // Table Header
                        paint.setTextSize(14);
                        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                        canvas.drawText("Student Name", 50, y, paint);
                        canvas.drawText("Absent", 220, y, paint);
                        canvas.drawText("Present", 300, y, paint);
                        canvas.drawText("Present %", 400, y, paint);
                        y += 25;

                        paint.setTypeface(Typeface.DEFAULT);

                        for (DataSnapshot studentSnap : dataSnapshot.getChildren()) {
                            String name = studentSnap.child("name").getValue(String.class);
                            if (name == null) name = "N/A";
                            DataSnapshot attendanceSnap = studentSnap.child("attendance");

                            int total = 0, present = 0;
                            for (DataSnapshot daySnap : attendanceSnap.getChildren()) {
                                total++;

                                Object value = daySnap.getValue();

                                if (value instanceof Boolean) {
                                    if ((Boolean) value) {
                                        present++; // true = present
                                    }
                                }
                            }

                            int absent = total - present;
                            int percent = (total > 0) ? (present * 100 / total) : 0;

                            canvas.drawText(name, 50, y, paint);
                            canvas.drawText(String.valueOf(absent), 220, y, paint);
                            canvas.drawText(String.valueOf(present), 300, y, paint);
                            canvas.drawText(percent + "%", 400, y, paint);
                            y += 20;

                            // Handle page overflow
                            if (y > 800) {
                                document.finishPage(page);
                                page = document.startPage(pageInfo);
                                canvas = page.getCanvas();
                                y = 50;
                            }
                        }

                        document.finishPage(page);

                        File filePath = new File(getExternalFilesDir(null), "AttendanceReport.pdf");
                        try {
                            document.writeTo(new FileOutputStream(filePath));
                            Toast.makeText(MarkAttendenceActivity.this, "Opening PDF...", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(FileProvider.getUriForFile(
                                            MarkAttendenceActivity.this,
                                            getApplicationContext().getPackageName() + ".provider",
                                            filePath),
                                    "application/pdf");

                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(MarkAttendenceActivity.this, "No PDF viewer installed.", Toast.LENGTH_LONG).show();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        document.close();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PDF", "Error reading student data", error.toException());
                    }

                });

    }

}



