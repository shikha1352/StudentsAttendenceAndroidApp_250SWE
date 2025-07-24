package com.example.studentsattendence;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.firebase.database.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AttendanceReportGenerator {
    private final FirebaseDatabase database;
    private final Context context;

    public AttendanceReportGenerator(Context context, FirebaseDatabase db) {
        this.context = context;
        this.database = db;
    }

    public void generate(String grade) {
        DatabaseReference ref = database.getReference("students");
        ref.orderByChild("grade").equalTo(grade)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
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

                        paint.setTextSize(14);
                        canvas.drawText("Student Name", 50, y, paint);
                        canvas.drawText("Absent", 220, y, paint);
                        canvas.drawText("Present", 300, y, paint);
                        canvas.drawText("Present %", 400, y, paint);
                        y += 25;
                        paint.setTypeface(Typeface.DEFAULT);

                        for (DataSnapshot studentSnap : snapshot.getChildren()) {
                            String name = studentSnap.child("name").getValue(String.class);
                            DataSnapshot attendanceSnap = studentSnap.child("attendance");

                            int total = 0, present = 0;
                            for (DataSnapshot daySnap : attendanceSnap.getChildren()) {
                                total++;
                                Boolean val = daySnap.getValue(Boolean.class);
                                if (val != null && val) present++;
                            }

                            int absent = total - present;
                            int percent = total > 0 ? (present * 100 / total) : 0;

                            canvas.drawText(name != null ? name : "N/A", 50, y, paint);
                            canvas.drawText(String.valueOf(absent), 220, y, paint);
                            canvas.drawText(String.valueOf(present), 300, y, paint);
                            canvas.drawText(percent + "%", 400, y, paint);
                            y += 20;
                        }

                        document.finishPage(page);
                        File file = new File(context.getExternalFilesDir(null), "AttendanceReport.pdf");
                        try {
                            document.writeTo(new FileOutputStream(file));
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                            intent.setDataAndType(uri, "application/pdf");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(intent);
                        } catch (IOException e) {
                            Toast.makeText(context, "Error creating PDF", Toast.LENGTH_SHORT).show();
                        }
                        document.close();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(context, "Failed to generate report", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
