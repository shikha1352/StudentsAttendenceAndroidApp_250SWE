package com.example.studentsattendence;

import java.util.HashMap;
import java.util.Map;

public class StudentInfo {
    private String name;
    private String address;
    private ParentInfo father;
    private ParentInfo mother;
    private GuardianInfo guardian;
    private String landPhone;
    private String weight;
    private String height;
    private String studentId;
    public String imageUri;

    public StudentInfo(String name, String address, ParentInfo father, ParentInfo mother,
                       GuardianInfo guardian, String landPhone, String weight, String height,
                       String studentId, String imageUri) {
        this.name = name;
        this.address = address;
        this.father = father;
        this.mother = mother;
        this.guardian = guardian;
        this.landPhone = landPhone;
        this.weight = weight;
        this.height = height;
        this.studentId = studentId;
        this.imageUri = imageUri;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("address", address);
        map.put("father", father.toMap());
        map.put("mother", mother.toMap());
        map.put("guardian", guardian.toMap());
        map.put("landPhone", landPhone);
        map.put("weight", weight);
        map.put("height", height);
        map.put("studentId", studentId);
        map.put("imageUri", imageUri);
        return map;
    }
}
