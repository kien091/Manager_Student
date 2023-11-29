package com.example.MidtermAndroid.Student.Certificate;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Certificate implements Serializable {
    private String uid;
    private String name;
    private String date;
    private String issuer;

    public Certificate(String uid, String name, String date, String issuer) {
        this.uid = uid;
        this.name = name;
        this.date = date;
        this.issuer = issuer;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @NonNull
    @Override
    public String toString() {
        return "Certificate{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", issuer='" + issuer + '\'' +
                '}';
    }
}
