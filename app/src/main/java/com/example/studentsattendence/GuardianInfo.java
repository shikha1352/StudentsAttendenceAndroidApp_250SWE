package com.example.studentsattendence;

import java.util.HashMap;
import java.util.Map;

public class GuardianInfo {
    private String name;
    private String mobile;
    private String occupation;
    private String address;
    private String email;
    private String userID;

    public GuardianInfo(String name, String mobile, String occupation, String address,
                        String email, String userID) {
        this.name = name;
        this.mobile = mobile;
        this.occupation = occupation;
        this.address = address;
        this.email = email;
        this.userID = userID;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("mobile", mobile);
        map.put("occupation", occupation);
        map.put("address", address);
        map.put("email", email);
        map.put("userID", userID);
        return map;
    }
}
