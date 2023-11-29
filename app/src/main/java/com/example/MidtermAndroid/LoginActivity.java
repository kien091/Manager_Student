package com.example.MidtermAndroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.MidtermAndroid.Student.StudentActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore database;
    TextInputEditText ed_email, ed_password;
    Button btn_login;
    TextView tv_error;
    TextInputLayout layout_password;
    private static String role;
    private static String userUId;

    public static String getRole() {
        return role;
    }

    public static String getUserUId() {
        return userUId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        ed_email = findViewById(R.id.ed_email);
        ed_password = findViewById(R.id.ed_password);
        btn_login = findViewById(R.id.btn_login);
        tv_error = findViewById(R.id.tv_error);
        layout_password = findViewById(R.id.layout_password);

        btn_login.setOnClickListener(v -> {
            String email = Objects.requireNonNull(ed_email.getText()).toString();
            String password = Objects.requireNonNull(ed_password.getText()).toString();
            loginUser(email, password);
        });
    }

    @SuppressLint("SetTextI18n")
    private void loginUser(String email, String password) {
        HashMap<String, Object> historyLogin = new HashMap<>();
        historyLogin.put("timestamp", FieldValue.serverTimestamp());

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        FirebaseUser user = auth.getCurrentUser();
                        String userId = Objects.requireNonNull(user).getUid();

                        userUId = userId;

                        database.collection("users").document(userId)
                                .collection("history")
                                .add(historyLogin);

                        database.collection("users").document(userId)
                                        .get().addOnSuccessListener(documentSnapshot -> {
                                            if(documentSnapshot.exists()){
                                                role = documentSnapshot.getString("role");
                                                String status = documentSnapshot.getString("status");
                                                if(Objects.requireNonNull(status).equalsIgnoreCase("normal"))
                                                    startActivity(new Intent(LoginActivity.this, StudentActivity.class));
                                                else
                                                    tv_error.setText("Your account is locked!");
                                            }
                                        });

                    }else{
                        tv_error.setText("Login failed!");
                    }
                });
    }
}