package com.example.MidtermAndroid.Student;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.MidtermAndroid.LoginActivity;
import com.example.MidtermAndroid.ProfileActivity;
import com.example.MidtermAndroid.R;
import com.example.MidtermAndroid.User.UserActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StudentActivity extends AppCompatActivity {
    FirebaseFirestore database;
    EditText ed_search;
    RecyclerView rcv;
    StudentAdapter adapter;
    ArrayList<Student> students;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        database = FirebaseFirestore.getInstance();

        ed_search = findViewById(R.id.ed_search);
        rcv = findViewById(R.id.recycler_view);

        students = new ArrayList<>();
        adapter = new StudentAdapter(this, students, LoginActivity.getRole());
        rcv.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rcv.addItemDecoration(dividerItemDecoration);

        rcv.setLayoutManager(new LinearLayoutManager(this));
        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void afterTextChanged(Editable s) {
                String search = ed_search.getText().toString().toLowerCase();

                ArrayList<Student> clone = new ArrayList<>();

                clone = (ArrayList<Student>) students.stream()
                        .filter(student -> student.getName().toLowerCase().contains(search)
                                || student.getStudentID().toLowerCase().contains(search)
                                || student.getFaculty().toLowerCase().contains(search)
                                || student.getGrade().toLowerCase().contains(search))
                        .collect(Collectors.toList());
                adapter.setStudents(clone);
                adapter.notifyDataSetChanged();
            }
        });

        loadStudentFromFireStore();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_option_menu, menu);

        if(LoginActivity.getRole().equals("employee")){
            menu.removeItem(R.id.i_save);
        }

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.i_user:
                startActivity(new Intent(getApplicationContext(), UserActivity.class));
                break;
            case R.id.i_add:
                Intent intent = new Intent(getApplicationContext(), ModifyStudentActivity.class);
                intent.putExtra("action", "add");
                startActivity(intent);
                break;
            case R.id.i_profile:
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                break;
            case R.id.i_sort_name:
                students = (ArrayList<Student>) students.stream()
                        .sorted(Comparator.comparing(Student::getName))
                        .collect(Collectors.toList());
                adapter.setStudents(students);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
                break;
            case R.id.i_sort_studentID:
                students = (ArrayList<Student>) students.stream()
                        .sorted(Comparator.comparing(Student::getStudentID))
                        .collect(Collectors.toList());
                adapter.setStudents(students);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
                break;
            case R.id.i_sort_faculty:
                students = (ArrayList<Student>) students.stream()
                        .sorted(Comparator.comparing(Student::getFaculty))
                        .collect(Collectors.toList());
                adapter.setStudents(students);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
                break;
            case R.id.i_sort_grade:
                students = (ArrayList<Student>) students.stream()
                        .sorted(Comparator.comparing(Student::getGrade))
                        .collect(Collectors.toList());
                adapter.setStudents(students);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
                break;
            case R.id.i_import:
                // write import (delete all data in firestore)
                break;
            case R.id.i_export:
                exportToCSV(students, "students.csv");
                runOnUiThread(() -> {
                    Toast.makeText(this, "Success to export students", Toast.LENGTH_SHORT).show();
                });
                break;
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), ModifyStudentActivity.class);
        switch (item.getItemId()){
            case R.id.i_detail:
                intent.putExtra("action", "view");
                intent.putExtra("student", adapter.getStudent());
                startActivity(intent);
                break;
            case R.id.i_delete:
                database.collection("students")
                        .document(adapter.getStudent().getUid())
                        .delete()
                        .addOnSuccessListener(unused ->
                                Toast.makeText(getApplicationContext(), "Success to delete student!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(getApplicationContext(), "Cannot delete student!", Toast.LENGTH_SHORT).show());
                break;
            case R.id.i_edit:
                intent.putExtra("action", "edit");
                intent.putExtra("student", adapter.getStudent());
                startActivity(intent);
                break;
        }
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadStudentFromFireStore(){
        students.clear();
        database.collection("students").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        String uid = document.getId();
                        String name = document.getString("name");
                        String dob = document.getString("dob");
                        String gender = document.getString("gender");
                        String phone = document.getString("phone");
                        String studentID = document.getString("studentID");
                        String grade = document.getString("grade");
                        String faculty = document.getString("faculty");
                        String major = document.getString("major");

                        Student student = new Student(uid, name, dob, gender, phone,
                                studentID, grade, faculty, major);
                        students.add(student);
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                })
                .addOnFailureListener(
                        e -> Toast.makeText(getApplicationContext(),
                                "Cannot load students!",
                                Toast.LENGTH_SHORT).show());
    }

    public void exportToCSV(List<Student> students, String fileName) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath() + File.separator + fileName;

        try (OutputStream outputStream = new FileOutputStream(filePath);
             Writer fileWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            fileWriter.append("UID,Name,DOB,Gender,Phone,StudentID,Grade,Faculty,Major\n");

            for (Student student : students) {
                fileWriter.append(student.getUid()).append(",")
                        .append(student.getName()).append(",")
                        .append(student.getDob()).append(",")
                        .append(student.getGender()).append(",")
                        .append(student.getPhone()).append(",")
                        .append(student.getStudentID()).append(",")
                        .append(student.getGrade()).append(",")
                        .append(student.getFaculty()).append(",")
                        .append(student.getMajor()).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}