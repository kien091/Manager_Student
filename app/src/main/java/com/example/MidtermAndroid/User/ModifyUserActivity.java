package com.example.MidtermAndroid.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MidtermAndroid.R;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ModifyUserActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST_CODE = 100;
    private String imageUrl = "";
    private String action = "view";
    FirebaseAuth auth;
    FirebaseFirestore database;
    User user;
    ImageView iv_avatar;
    Button btn_camera;
    TextInputEditText ed_name, ed_dob, ed_phone, ed_email, ed_password, ed_role, ed_status;
    TextInputLayout layout_name, layout_dob, layout_phone, layout_email,
            layout_password, layout_role, layout_status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_user);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        iv_avatar = findViewById(R.id.iv_avatar);
        btn_camera = findViewById(R.id.btn_camera);
        ed_name = findViewById(R.id.ed_name);
        ed_dob = findViewById(R.id.ed_dob);
        ed_phone = findViewById(R.id.ed_phone);
        ed_email = findViewById(R.id.ed_email);
        ed_password = findViewById(R.id.ed_password);
        ed_role = findViewById(R.id.ed_role);
        ed_status = findViewById(R.id.ed_status);

        layout_name = findViewById(R.id.layout_name);
        layout_dob = findViewById(R.id.layout_dob);
        layout_phone = findViewById(R.id.layout_phone);
        layout_email = findViewById(R.id.layout_email);
        layout_password = findViewById(R.id.layout_password);
        layout_role = findViewById(R.id.layout_role);
        layout_status = findViewById(R.id.layout_status);

        Intent intent = getIntent();
        if(intent.hasExtra("action")) {
            action = intent.getStringExtra("action");
            if(action.equals("edit") || action.equals("view")){
                user = (User) intent.getSerializableExtra("user");

                if(user.getAvatar().isEmpty()){
                    iv_avatar.setImageResource(R.drawable.baseline_person_24);
                }else{
                    imageUrl = user.getAvatar();
                    iv_avatar.setImageURI(Uri.parse(user.getAvatar()));
                }
                ed_name.setText(user.getName());
                ed_dob.setText(user.getDob());
                ed_phone.setText(user.getPhone());
                ed_email.setText(user.getEmail());
                ed_password.setText("123456"); // set example password
                ed_role.setText(user.getRole());
                ed_status.setText(user.getStatus());

                if(action.equals("view")){
                    ed_name.setEnabled(false);
                    ed_dob.setEnabled(false);
                    ed_phone.setEnabled(false);
                    ed_email.setEnabled(false);
                    ed_password.setEnabled(false);
                    ed_role.setEnabled(false);
                    ed_status.setEnabled(false);
                    btn_camera.setVisibility(View.GONE);
                }
            }
        }

        btn_camera.setOnClickListener(v -> {
            Intent galleryIntent =
                    new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        });

        ed_name.setOnClickListener(v -> {
            if(layout_name.getError() != null)
                layout_name.setError(null);
        });
        ed_dob.setOnClickListener(v -> {
            if(layout_dob.getError() != null)
                layout_dob.setError(null);
        });
        ed_phone.setOnClickListener(v -> {
            if(layout_phone.getError() != null)
                layout_phone.setError(null);
        });
        ed_email.setOnClickListener(v -> {
            if(layout_email.getError() != null)
                layout_email.setError(null);
        });
        ed_password.setOnClickListener(v -> {
            if(layout_password.getError() != null)
                layout_password.setError(null);
        });
        ed_role.setOnClickListener(v -> {
            if(layout_role.getError() != null)
                layout_role.setError(null);
            chooseRole();
        });
        ed_status.setOnClickListener(v -> {
            if(layout_status.getError() != null)
                layout_status.setError(null);
            chooseStatus();
        });
    }
    private void chooseRole(){
        final String[] options = {"manager", "employee"};
        int default_select_item = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setTitle("Select place").
                setSingleChoiceItems(options,  default_select_item,(dialog, which) -> {
                    ed_role.setText(options[which]);
                    dialog.dismiss();
                });
        builder.create().show();

        if(ed_role.getError() != null)
            ed_role.setError(null);
    }
    private void chooseStatus(){
        final String[] options = {"Normal", "Locked"};
        int default_select_item = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this).
                setTitle("Select place").
                setSingleChoiceItems(options,  default_select_item,(dialog, which) -> {
                    ed_status.setText(options[which]);
                    dialog.dismiss();
                });
        builder.create().show();

        if(ed_status.getError() != null)
            ed_role.setError(null);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String imagePath = getImagePath(selectedImageUri);
            iv_avatar.setImageURI(selectedImageUri);

            imageUrl = imagePath;
        }
    }
    private String getImagePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return uri.getPath();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_modify, menu);

        if(action.equals("view"))
            menu.removeItem(R.id.i_save);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        HashMap<String, Object> user = new HashMap<>();
        user.put("name", ed_name.getText().toString());
        user.put("avatar", imageUrl);
        user.put("dob", ed_dob.getText().toString());
        user.put("phone", ed_phone.getText().toString());
        user.put("email", ed_email.getText().toString());
        user.put("role", ed_role.getText().toString());
        user.put("status", ed_status.getText().toString());

        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
        switch (item.getItemId()){
            case R.id.i_save:
                if(ed_name.getText().toString().isEmpty()){
                    layout_name.setError("Please enter user name!");
                    break;
                } else if (ed_dob.getText().toString().isEmpty()) {
                    layout_dob.setError("Please enter user date of birth!");
                    break;
                } else if (ed_phone.getText().toString().isEmpty()) {
                    layout_phone.setError("Please enter user phone!");
                    break;
                } else if (ed_email.getText().toString().isEmpty()) {
                    layout_email.setError("Please enter user email!");
                    break;
                } else if (ed_role.getText().toString().isEmpty()) {
                    layout_role.setError("Please choose user role!");
                    break;
                } else if (ed_status.getText().toString().isEmpty()) {
                    layout_status.setError("Please choose user status!");
                    break;
                }

                if(action.equals("add")){
                    registerUser(ed_email.getText().toString(),
                            ed_password.getText().toString(), user);
                    database.collection("users")
                            .add(user)
                            .addOnSuccessListener(documentReference ->
                                    runOnUiThread(() -> Toast.makeText(
                                    getApplicationContext()
                                    , "Success to add a new user!"
                                    , Toast.LENGTH_SHORT).show()))
                            .addOnFailureListener(e -> runOnUiThread(() -> Toast.makeText(
                                    getApplicationContext()
                                    , "Fail to add a new user!"
                                    , Toast.LENGTH_SHORT).show()));
                } else if (action.equals("edit")) {
                    database.collection("users")
                            .document(this.user.getUid())
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                runOnUiThread(() -> Toast.makeText(
                                        getApplicationContext()
                                        , "Success to update user!"
                                        , Toast.LENGTH_SHORT).show());
                            });
                }
                startActivity(intent);
                break;
                case android.R.id.home:
                    finish();
                    break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void registerUser(String email, String password, HashMap<String, Object> data) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        String userId = user.getUid();

                        Log.e("TAG", "registerUser: " + userId);
                        database.collection("users")
                                .document(userId)
                                .set(data)
                                .addOnSuccessListener(unused -> {
                                    database.collection("users")
                                            .document(userId)
                                            .collection("history")
                                            .add(new HashMap<>());
                                });
                    }
                });
    }
}