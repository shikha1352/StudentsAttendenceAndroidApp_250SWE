package com.example.studentsattendence;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Student> {
    private final ArrayList<Student> students;
    private final Context context;

    public CustomAdapter(Context context, ArrayList<Student> students) {
        super(context, 0, students);
        this.context = context;
        this.students = students;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Student student = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.sample_view, parent, false);
        }

        TextView name = convertView.findViewById(R.id.txtName);
        CheckBox checkbox = convertView.findViewById(R.id.checkBox);

        name.setText(student.getName());
        checkbox.setChecked(student.isAssigned());

        // Toggle assigned state on checkbox click
        checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> student.setAssigned(isChecked));

        return convertView;
    }

    public ArrayList<String> getCheckedIDs() {
        ArrayList<String> selectedIDs = new ArrayList<>();
        for (Student student : students) {
            if (student.isAssigned()) {
                selectedIDs.add(student.getStudentID());
            }
        }
        return selectedIDs;
    }
}