package com.example.MidtermAndroid.User;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.MidtermAndroid.LoginActivity;
import com.example.MidtermAndroid.R;
import com.example.MidtermAndroid.Student.StudentActivity;
import com.example.MidtermAndroid.User.History.HistoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {
    FirebaseFirestore database;
    RecyclerView rv_user;
    ArrayList<User> users;
    UserAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        database = FirebaseFirestore.getInstance();

        rv_user = findViewById(R.id.rv_user);

        users = new ArrayList<>();
        adapter = new UserAdapter(this, users);
        rv_user.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rv_user.addItemDecoration(dividerItemDecoration);

        rv_user.setLayoutManager(new LinearLayoutManager(this));

        loadUserFromFirebase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_option_menu, menu);

        menu.removeItem(R.id.i_user);
        menu.removeItem(R.id.i_sort);
        menu.removeItem(R.id.i_import);
        menu.removeItem(R.id.i_export);

        if(LoginActivity.getRole().equals("manager") || LoginActivity.getRole().equals("employee")){
            menu.removeItem(R.id.i_add);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(this, ModifyUserActivity.class);
        switch (item.getItemId()){
            case R.id.i_student:
                startActivity(new Intent(this, StudentActivity.class));
                finish();
                break;
            case R.id.i_add:
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
                                Intent intent1 = new Intent(this, ModifyUserActivity.class);
                                intent1.putExtra("action", "profile");
                                intent1.putExtra("user", user);
                                startActivity(intent1);
                            }
                        });
                break;
            case R.id.i_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(this, ModifyUserActivity.class);
        switch (item.getItemId()){
            case R.id.i_detail:
                intent.putExtra("action", "view");
                intent.putExtra("user", adapter.getUser());
                startActivity(intent);
                break;
            case R.id.i_edit:
                intent.putExtra("action", "edit");
                intent.putExtra("user", adapter.getUser());
                startActivity(intent);
                break;
            case R.id.i_delete:
                database.collection("users")
                        .document(adapter.getUser().getUid())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            loadUserFromFirebase();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(),
                                    "Delete user successfully!",
                                    Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e ->
                                Toast.makeText(getApplicationContext(),
                                        "Cannot delete user!",
                                        Toast.LENGTH_SHORT).show());
                break;
            case R.id.i_history:
                intent = new Intent(this, HistoryActivity.class);
                intent.putExtra("user", adapter.getUser());
                startActivity(intent);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadUserFromFirebase() {
        users.clear();
        database.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document: queryDocumentSnapshots){
                String uid = document.getString("uid");
                String name = document.getString("name");
                String avatar = document.getString("avatar");
                String dob = document.getString("dob");
                String phone = document.getString("phone");
                String email = document.getString("email");
                String role = document.getString("role");
                String status = document.getString("status");

                User user = new User(uid, name, avatar, dob, phone, email, role, status);
                users.add(user);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }}).addOnFailureListener(
                        e -> Toast.makeText(getApplicationContext(),
                                "Cannot load users!",
                                Toast.LENGTH_SHORT).show());
    }
}