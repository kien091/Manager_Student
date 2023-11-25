package com.example.MidtermAndroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore database;
    TextInputEditText ed_email, ed_password;
    Button btn_login;
    TextView tv_register, tv_error;
    TextInputLayout layout_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        ed_email = findViewById(R.id.ed_email);
        ed_password = findViewById(R.id.ed_password);
        btn_login = findViewById(R.id.btn_login);
        tv_register = findViewById(R.id.tv_register);
        tv_register.setMovementMethod(LinkMovementMethod.getInstance());
        tv_error = findViewById(R.id.tv_error);
        layout_password = findViewById(R.id.layout_password);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ed_email.getText().toString();
                String password = ed_password.getText().toString();
                loginUser(email, password);
            }
        });

        tv_register.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loginUser(String email, String password) {
        HashMap<String, Object> historyLogin = new HashMap<>();
        historyLogin.put("timestamp", FieldValue.serverTimestamp());

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        FirebaseUser user = auth.getCurrentUser();
                        String userId = user.getUid();

                        database.collection("users").document(userId)
                                .collection("history")
                                .add(historyLogin);

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }else{
                        Exception exception = task.getException();
                        if(exception instanceof FirebaseAuthInvalidUserException)
                            tv_error.setText("Email isn't exist!");
                        else if (exception instanceof FirebaseAuthInvalidCredentialsException)
                            tv_error.setText("Wrong password!");
                        else
                            tv_error.setText("Login failed!");
                    }
                });
    }
}