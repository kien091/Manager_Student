package com.example.MidtermAndroid.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.MidtermAndroid.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class StudentActivity extends AppCompatActivity {
    FirebaseFirestore database;
    SharedPreferences sharedPreferences;
    EditText ed_search;
    RecyclerView rcv;
    StudentAdapter adapter;
    ArrayList<Student> students;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        database = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("templateData", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "employee");

        ed_search = findViewById(R.id.ed_search);
        rcv = findViewById(R.id.recycler_view);

        students = new ArrayList<>();
        adapter = new StudentAdapter(this, students);
        rcv.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rcv.addItemDecoration(dividerItemDecoration);

        rcv.setLayoutManager(new LinearLayoutManager(this));

        loadStudentFromFireStore();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_option_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    private void loadStudentFromFireStore(){
        database.collection("students").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getApplicationContext(), "Cannot load students!", Toast.LENGTH_SHORT));
    }
}