package com.example.MidtermAndroid.Student;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.MidtermAndroid.LoginActivity;
import com.example.MidtermAndroid.R;
import com.example.MidtermAndroid.Student.Certificate.CertificateActivity;
import com.example.MidtermAndroid.User.ModifyUserActivity;
import com.example.MidtermAndroid.User.User;
import com.example.MidtermAndroid.User.UserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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

                ArrayList<Student> clone;

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

        menu.removeItem(R.id.i_student);

        if (LoginActivity.getRole().equals("employee")) {
            menu.removeItem(R.id.i_add);
            menu.removeItem(R.id.i_import);
            menu.removeItem(R.id.i_export);
        }
        return true;
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
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
                database.collection("users")
                        .document(LoginActivity.getUserUId())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if(documentSnapshot.exists()){
                                String name = documentSnapshot.getString("name");
                                String avatar = documentSnapshot.getString("avatar");
                                String dob = documentSnapshot.getString("dob");
                                String phone = documentSnapshot.getString("phone");
                                String email = documentSnapshot.getString("email");
                                String role = documentSnapshot.getString("role");
                                String status = documentSnapshot.getString("status");

                                User user = new User(LoginActivity.getUserUId(), name, avatar, dob, phone, email, role, status);
                                Intent intent1 = new Intent(getApplicationContext(), ModifyUserActivity.class);
                                intent1.putExtra("action", "profile");
                                intent1.putExtra("user", user);
                                startActivity(intent1);
                            }
                        });
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setMessage("It will be delete all current data?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            importFromCSV("students.csv", students);

                            for(Student student: students){
                                database.collection("students")
                                        .document(student.getUid())
                                        .set(student)
                                        .addOnSuccessListener(unused -> runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                                "Success to import students", Toast.LENGTH_SHORT).show()))
                                        .addOnFailureListener(e -> runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                                "Cannot import students", Toast.LENGTH_SHORT).show()));
                            }

                        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                builder.create().show();
                break;
            case R.id.i_export:
                exportToCSV(students, "students.csv");
                runOnUiThread(() -> Toast.makeText(this, "Success to export students", Toast.LENGTH_SHORT).show());
                break;
            case R.id.i_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
        }
        return true;
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
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
                        .addOnSuccessListener(unused -> {
                            loadStudentFromFireStore();
                            adapter.notifyDataSetChanged();
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                    "Success to delete student!", Toast.LENGTH_SHORT).show());})
                        .addOnFailureListener(e ->
                                Toast.makeText(getApplicationContext(),
                                        "Cannot delete student!",
                                        Toast.LENGTH_SHORT).show());
                break;
            case R.id.i_edit:
                intent.putExtra("action", "edit");
                intent.putExtra("student", adapter.getStudent());
                startActivity(intent);
                break;
            case R.id.i_certificate:
                Intent certificateIntent = new Intent(getApplicationContext(), CertificateActivity.class);
                certificateIntent.putExtra("student", adapter.getStudent());
                startActivity(certificateIntent);
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

        try (OutputStream outputStream = new FileOutputStream(filePath, false);
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
    @SuppressLint("NotifyDataSetChanged")
    public void importFromCSV(String fileName, ArrayList<Student> students){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath() + File.separator + fileName;

        File file = new File(filePath);
        if(!file.exists()) {
            Toast.makeText(this, fileName + " isn't exist in folder", Toast.LENGTH_SHORT).show();
        }
        else {
            students.clear();

            try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
                String line = bufferedReader.readLine();

                while ((line = bufferedReader.readLine()) != null){
                    String[] data = line.split(",");
                    if (data.length == 9) {
                        String uid = data[0];
                        String name = data[1];
                        String dob = data[2];
                        String gender = data[3];
                        String phone = data[4];
                        String studentID = data[5];
                        String grade = data[6];
                        String faculty = data[7];
                        String major = data[8];

                        Student student = new Student(uid, name, dob, gender, phone,
                                studentID, grade, faculty, major);

                        students.add(student);
                    }
                }
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}