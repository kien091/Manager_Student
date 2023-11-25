package com.example.MidtermAndroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore database;
    TextInputEditText ed_name, ed_age, ed_phone, ed_email, ed_password;
    Button btn_register;
    TextView tv_login, tv_error;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        ed_name = findViewById(R.id.ed_name);
        ed_age = findViewById(R.id.ed_age);
        ed_phone = findViewById(R.id.ed_phone);
        ed_email = findViewById(R.id.ed_email);
        ed_password = findViewById(R.id.ed_password);
        btn_register = findViewById(R.id.btn_register);
        tv_login = findViewById(R.id.tv_login);
        tv_login.setMovementMethod(LinkMovementMethod.getInstance());
        tv_error = findViewById(R.id.tv_error);

        btn_register.setOnClickListener(v -> {
            String name = ed_name.getText().toString();
            int age = Integer.parseInt(ed_age.getText().toString());
            String phone = ed_phone.getText().toString();
            String email = ed_email.getText().toString();
            String password = ed_password.getText().toString();

            registerUser(name, age, phone, email, password);
        });

        tv_login.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void registerUser(String name, int age, String phone, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            String userId = user.getUid();

                            HashMap<String, Object> data = new HashMap<>();
                            data.put("email", email);
                            data.put("name", name);
                            data.put("age", age);
                            data.put("phone", phone);
                            data.put("status", "normal");
                            data.put("role", "employee");

                            HashMap<String, Object> historyLogin = new HashMap<>();
                            historyLogin.put("timestamp", FieldValue.serverTimestamp());

                            database.collection("users").document(userId)
                                    .set(data)
                                    .addOnSuccessListener(unused -> {
                                        database.collection("users")
                                                .document(userId)
                                                .collection("history")
                                                .add(historyLogin);

                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    }).addOnFailureListener(e -> tv_error.setText("Can't add information user!"));
                        }else{
                            Exception exception = task.getException();
                            if(exception instanceof FirebaseAuthUserCollisionException)
                                tv_error.setText("Email already exist!");
                            else
                                tv_error.setText("Register failed!");
                        }
                    }
                });
    }
}