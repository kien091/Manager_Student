package com.example.MidtermAndroid.User;

public class User {
    private String Uid;
    private String name;
    private String dob;
    private String phone;
    private String email;
    private String role;
    private String status;

    public User(String uid, String name, String dob, String phone, String email, String role, String status) {
        Uid = uid;
        this.name = name;
        this.dob = dob;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.status = status;
    }
}
