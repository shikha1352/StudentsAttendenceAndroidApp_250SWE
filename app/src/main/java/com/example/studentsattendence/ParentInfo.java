package com.example.studentsattendence;

import java.util.HashMap;
import java.util.Map;

public class ParentInfo {
    private String name;
    private String mobile;
    private String occupation;
    private String email;

    public ParentInfo(String name, String mobile, String occupation, String email) {
        this.name = name;
        this.mobile = mobile;
        this.occupation = occupation;
        this.email = email;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("mobile", mobile);
        map.put("occupation", occupation);
        map.put("email", email);
        return map;
    }
}
