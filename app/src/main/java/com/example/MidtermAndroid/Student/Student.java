package com.example.MidtermAndroid.Student;

import java.io.Serializable;

public class Student implements Serializable {
    private String Uid;
    private String name;
    private String dob;
    private String gender;
    private String phone;
    private String studentID;
    private String grade;
    private String faculty;
    private String major;

    public Student(String uid, String name, String dob, String gender, String phone,
                   String studentID, String grade, String faculty, String major) {
        Uid = uid;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.studentID = studentID;
        this.grade = grade;
        this.faculty = faculty;
        this.major = major;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    @Override
    public String toString() {
        return "Student{" +
                "Uid='" + Uid + '\'' +
                ", name='" + name + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", studentID='" + studentID + '\'' +
                ", grade='" + grade + '\'' +
                ", faculty='" + faculty + '\'' +
                ", major='" + major + '\'' +
                '}';
    }
}
