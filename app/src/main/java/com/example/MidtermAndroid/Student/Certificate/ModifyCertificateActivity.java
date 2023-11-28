package com.example.MidtermAndroid.Student.Certificate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.MidtermAndroid.R;
import com.example.MidtermAndroid.Student.Student;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ModifyCertificateActivity extends AppCompatActivity {
    FirebaseFirestore database;
    TextInputEditText ed_name, ed_date, ed_issuer;
    TextInputLayout layout_name, layout_date, layout_issuer;
    String action = "add";
    String student_uid;
    Certificate certificate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_certificate);

        database = FirebaseFirestore.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ed_name = findViewById(R.id.ed_name);
        ed_date = findViewById(R.id.ed_date);
        ed_issuer = findViewById(R.id.ed_issuer);
        layout_name = findViewById(R.id.layout_name);
        layout_date = findViewById(R.id.layout_date);
        layout_issuer = findViewById(R.id.layout_issuer);

        Intent intent = getIntent();
        if(intent.hasExtra("action")) {
            action = intent.getStringExtra("action");
            student_uid = intent.getStringExtra("student_uid");
        }

        if(action.equals("edit")){
            certificate = (Certificate) intent.getSerializableExtra("certificate");
            ed_name.setText(certificate.getName());
            ed_date.setText(certificate.getDate());
            ed_issuer.setText(certificate.getIssuer());
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_modify, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        HashMap<String, Object> certificate = new HashMap<>();
        certificate.put("name", ed_name.getText().toString());
        certificate.put("date", ed_date.getText().toString());
        certificate.put("issuer", ed_issuer.getText().toString());

        switch (item.getItemId()) {
            case R.id.i_save:
                if(ed_name.getText().toString().isEmpty()){
                    layout_name.setError("Please enter certificate name");
                    return false;
                } else if (ed_date.getText().toString().isEmpty()){
                    layout_date.setError("Please enter certificate date");
                    return false;
                } else if (ed_issuer.getText().toString().isEmpty()){
                    layout_issuer.setError("Please enter certificate issuer");
                    return false;
                }

                if(action.equals("add")){
                    database.collection("students")
                            .document(student_uid)
                            .collection("certificates")
                            .add(certificate);
                    runOnUiThread(() -> Toast.makeText(
                            getApplicationContext(),
                            "Add certificate successfully!",
                            Toast.LENGTH_SHORT).show());
                } else if(action.equals("edit")){
                    database.collection("students")
                            .document(student_uid)
                            .collection("certificates")
                            .document(this.certificate.getUid())
                            .set(certificate);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                            "Update certificate successfully!",
                            Toast.LENGTH_SHORT).show());
                }
                finish();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}