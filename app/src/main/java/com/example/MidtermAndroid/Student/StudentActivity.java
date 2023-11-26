package com.example.MidtermAndroid.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.example.MidtermAndroid.LoginActivity;
import com.example.MidtermAndroid.ProfileActivity;
import com.example.MidtermAndroid.R;
import com.example.MidtermAndroid.User.UserActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

            @Override
            public void afterTextChanged(Editable s) {
                String search = ed_search.getText().toString().toLowerCase();
                students = (ArrayList<Student>) students.stream()
                        .filter(student -> student.getName().toLowerCase().contains(search)
                                || student.getStudentID().toLowerCase().contains(search)
                                || student.getFaculty().toLowerCase().contains(search)
                                || student.getGrade().toLowerCase().contains(search))
                        .collect(Collectors.toList());

                adapter.setStudents(students);
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
            case R.id.i_sort:
                showSortOption(item.getActionView());
                break;
            case R.id.i_import:
                // write import (delete all data in firestore)
                break;
            case R.id.i_export:
                // some thing here
                break;
        }
        return true;
    }

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
                                Toast.makeText(getApplicationContext(), "Xóa sinh viên thành công!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(getApplicationContext(), "Không thể sinh viên thành công!", Toast.LENGTH_SHORT).show());
                break;
            case R.id.i_edit:
                intent.putExtra("action", "edit");
                intent.putExtra("student", adapter.getStudent());
                startActivity(intent);
                break;
        }
        return true;
    }

    private void showSortOption(View actionView) {
        PopupMenu popupMenu = new PopupMenu(this, actionView);
        popupMenu.getMenuInflater().inflate(R.menu.student_option_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            Comparator<Student> comparator = null;

            switch (item.getItemId()){
                case R.id.i_sort_name:
                    comparator = (o1, o2) -> o1.getName().compareTo(o2.getName());
                    break;
                case R.id.i_sort_studentID:
                    comparator = (o1, o2) -> o1.getStudentID().compareTo(o2.getStudentID());
                    break;
                case R.id.i_sort_faculty:
                    comparator = (o1, o2) -> o1.getFaculty().compareTo(o2.getFaculty());
                    break;
                case R.id.i_sort_grade:
                    comparator = (o1, o2) -> o1.getGrade().compareTo(o2.getGrade());
                    break;
            }
            Collections.sort(students, comparator);
            runOnUiThread(() -> adapter.notifyDataSetChanged());

            return true;
        });
    }

    private void loadStudentFromFireStore(){
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
}