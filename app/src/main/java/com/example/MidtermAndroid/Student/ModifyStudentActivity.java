package com.example.MidtermAndroid.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.MidtermAndroid.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class ModifyStudentActivity extends AppCompatActivity {
    FirebaseFirestore database;
    TextInputEditText ed_name, ed_dob, ed_gender, ed_phone,
            ed_studentID, ed_grade, ed_faculty, ed_major;
    TextInputLayout layout_name, layout_dob, layout_gender, layout_phone,
            layout_studentID, layout_grade, layout_faculty, layout_major;
    String action = "";
    Student student;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_student);

        database = FirebaseFirestore.getInstance();

        ed_name = findViewById(R.id.ed_name);
        ed_dob = findViewById(R.id.ed_dob);
        ed_gender = findViewById(R.id.ed_gender);
        ed_phone = findViewById(R.id.ed_phone);
        ed_studentID = findViewById(R.id.ed_studentID);
        ed_grade = findViewById(R.id.ed_grade);
        ed_faculty = findViewById(R.id.ed_faculty);
        ed_major = findViewById(R.id.ed_major);

        layout_name = findViewById(R.id.layout_name);
        layout_dob = findViewById(R.id.layout_dob);
        layout_gender = findViewById(R.id.layout_gender);
        layout_phone = findViewById(R.id.layout_phone);
        layout_studentID = findViewById(R.id.layout_studentID);
        layout_grade = findViewById(R.id.layout_grade);
        layout_faculty = findViewById(R.id.layout_faculty);
        layout_major = findViewById(R.id.layout_major);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if(intent.hasExtra("action")){
            action = getIntent().getStringExtra("action");
        }

        if(intent.hasExtra("student")){
            student = (Student) intent.getSerializableExtra("student");

            ed_name.setText(student.getName());
            ed_dob.setText(student.getDob());
            ed_gender.setText(student.getGender());
            ed_phone.setText(student.getPhone());
            ed_studentID.setText(student.getStudentID());
            ed_grade.setText(student.getGrade());
            ed_faculty.setText(student.getFaculty());
            ed_major.setText(student.getMajor());
        }

        if(action.equals("view")){
            ed_name.setEnabled(false);
            ed_dob.setEnabled(false);
            ed_gender.setEnabled(false);
            ed_phone.setEnabled(false);
            ed_studentID.setEnabled(false);
            ed_grade.setEnabled(false);
            ed_faculty.setEnabled(false);
            ed_major.setEnabled(false);
        }

        ed_name.setOnClickListener(v -> {
            if(layout_name.getError() != null)
                layout_name.setError(null);
        });
        ed_dob.setOnClickListener(v -> {
            if(layout_dob.getError() != null)
                layout_dob.setError(null);
        });
        ed_gender.setOnClickListener(v -> {
            showOptionDialog();
            if(layout_gender.getError() != null)
                layout_gender.setError(null);
        });
        ed_phone.setOnClickListener(v -> {
            if(layout_phone.getError() != null)
                layout_phone.setError(null);
        });
        ed_studentID.setOnClickListener(v -> {
            if(layout_studentID.getError() != null)
                layout_studentID.setError(null);
        });
        ed_grade.setOnClickListener(v -> {
            if(layout_grade.getError() != null)
                layout_grade.setError(null);
        });
        ed_faculty.setOnClickListener(v -> {
            if(layout_faculty.getError() != null)
                layout_faculty.setError(null);
        });
        ed_major.setOnClickListener(v -> {
            if(layout_major.getError() != null)
                layout_major.setError(null);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_modify, menu);

        if(action.equals("view")){
            menu.removeItem(R.id.i_save);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        HashMap<String, Object> student = new HashMap<>();
        student.put("name", ed_name.getText().toString());
        student.put("dob", ed_dob.getText().toString());
        student.put("gender", ed_gender.getText().toString());
        student.put("phone", ed_phone.getText().toString());
        student.put("studentID", ed_studentID.getText().toString());
        student.put("grade", ed_grade.getText().toString());
        student.put("faculty", ed_faculty.getText().toString());
        student.put("major", ed_major.getText().toString());

        Intent intent = new Intent(getApplicationContext(), StudentActivity.class);
        switch (item.getItemId()){
            case R.id.i_save:
                if(ed_name.getText().toString().isEmpty()){
                    layout_name.setError("Please enter student name");
                    return false;
                }else if(ed_dob.getText().toString().isEmpty()){
                    layout_dob.setError("Please choose date of birth");
                    return false;
                }else if(ed_gender.getText().toString().isEmpty()){
                    layout_dob.setError("Please choose student gender");
                    return false;
                }else if(ed_phone.getText().toString().isEmpty()){
                    layout_dob.setError("Please enter student phone");
                    return false;
                }else if(ed_studentID.getText().toString().isEmpty()){
                    layout_dob.setError("Please enter student id");
                    return false;
                }else if(ed_grade.getText().toString().isEmpty()){
                    layout_dob.setError("Please enter student grade");
                    return false;
                }else if(ed_faculty.getText().toString().isEmpty()){
                    layout_dob.setError("Please enter student faculty");
                    return false;
                }else if(ed_major.getText().toString().isEmpty()){
                    layout_dob.setError("Please enter student major");
                    return false;
                }


                if(action.equals("add")){
                    database.collection("students").add(student);
                    runOnUiThread(() -> Toast.makeText(
                            getApplicationContext()
                            , "Success to add a new student!"
                            , Toast.LENGTH_SHORT).show());
                } else if (action.equals("edit")) {
                    database.collection("students")
                            .document(this.student.getUid()).set(student);
                    runOnUiThread(() -> Toast.makeText(
                            getApplicationContext()
                            , "Success to update student"
                            , Toast.LENGTH_SHORT).show());
                }
                startActivity(intent);
                finish();
                break;
            case android.R.id.home:
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }
    private void showOptionDialog(){
        final String[] options = {"Male", "Female"};
        int default_select_item = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setTitle("Select place").
                setSingleChoiceItems(options,  default_select_item,(dialog, which) -> {
                    ed_gender.setText(options[which]);
                    dialog.dismiss();
                });
        builder.create().show();

        if(layout_gender.getError() != null)
            layout_gender.setError(null);
    }
}