package com.example.MidtermAndroid.Student.Certificate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.example.MidtermAndroid.Student.Student;
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

public class CertificateActivity extends AppCompatActivity {
    FirebaseFirestore database;
    RecyclerView rv_certificates;
    CertificateAdapter adapter;
    ArrayList<Certificate> certificates;
    Student student;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);

        database = FirebaseFirestore.getInstance();

        rv_certificates = findViewById(R.id.rv_certificates);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        certificates = new ArrayList<>();
        adapter = new CertificateAdapter(this, certificates);
        rv_certificates.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rv_certificates.addItemDecoration(dividerItemDecoration);

        rv_certificates.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        if(intent.hasExtra("student")){
            student = (Student) intent.getSerializableExtra("student");
            getSupportActionBar().setTitle(student.getName());
            loadCertificates(student);
        }
    }

    private void loadCertificates(Student student) {
        certificates.clear();
        database.collection("students")
                .document(student.getUid())
                .collection("certificates")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String uid = document.getId();
                        String name = document.getString("name");
                        String date = document.getString("date");
                        String issuer = document.getString("issuer");

                        Certificate certificate = new Certificate(uid, name, date, issuer);
                        certificates.add(certificate);
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                        "Cannot load certificates!",
                        Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.student_option_menu, menu);

        menu.removeItem(R.id.i_student);
        menu.removeItem(R.id.i_user);
        menu.removeItem(R.id.i_profile);
        menu.removeItem(R.id.i_sort);
        menu.removeItem(R.id.i_logout);

        if(LoginActivity.getRole().equals("employee")){
            menu.removeItem(R.id.i_add);
            menu.removeItem(R.id.i_import);
            menu.removeItem(R.id.i_export);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.i_add:
                Intent intent = new Intent(this, ModifyCertificateActivity.class);
                intent.putExtra("action", "add");
                intent.putExtra("student_uid", student.getUid());
                startActivity(intent);
                break;
            case R.id.i_import:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setMessage("It will be delete all current certificate of " + student.getName() + " ?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            importFromCSV("certificate.csv", certificates);

                            for(Certificate certificate: certificates){
                                database.collection("students")
                                        .document(student.getUid())
                                        .collection("certificates")
                                        .document(certificate.getUid())
                                        .set(certificate)
                                        .addOnSuccessListener(unused -> runOnUiThread(() -> {
                                            Toast.makeText(getApplicationContext(),
                                                    "Success to import certificates", Toast.LENGTH_SHORT).show();
                                        }))
                                        .addOnFailureListener(e -> runOnUiThread(() -> {
                                            Toast.makeText(getApplicationContext(),
                                                    "Cannot import certificates", Toast.LENGTH_SHORT).show();
                                        }));
                            }
                        }).setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        });
                builder.create().show();
                break;
            case R.id.i_export:
                exportToCSV(certificates, "certificate.csv");
                runOnUiThread(() -> {
                    Toast.makeText(this, "Success to export certificates of " + student.getName(), Toast.LENGTH_SHORT).show();
                });
                break;
                case android.R.id.home:
                    finish();
                    break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void importFromCSV(String fileName, ArrayList<Certificate> certificates) {
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
        }else{
            try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
                certificates.clear();
                String line = bufferedReader.readLine();
                while ((line = bufferedReader.readLine()) != null){
                    String[] values = line.split(",");
                    String uid = values[0];
                    String name = values[1];
                    String issuer = values[2];
                    String date = values[3];

                    Certificate certificate = new Certificate(uid, name, date, issuer);
                    certificates.add(certificate);
                }
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportToCSV(ArrayList<Certificate> certificates, String fileName){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath() + File.separator + fileName;
        try (OutputStream outputStream = new FileOutputStream(filePath);
             Writer fileWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)){
            fileWriter.append("UID,Name,Issuer,Date\n");

            for (Certificate certificate : certificates) {
                fileWriter.append(certificate.getUid()).append(",")
                        .append(certificate.getName()).append(",")
                        .append(certificate.getIssuer()).append(",")
                        .append(certificate.getDate()).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.i_edit:
                Intent intent = new Intent(this, ModifyCertificateActivity.class);
                intent.putExtra("action", "edit");
                intent.putExtra("student_uid", student.getUid());
                intent.putExtra("certificate", adapter.getCertificate());
                startActivity(intent);
                break;
            case R.id.i_delete:
                database.collection("students")
                        .document(student.getUid())
                        .collection("certificates")
                        .document(adapter.getCertificate().getUid())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            loadCertificates(student);
                            adapter.notifyDataSetChanged();
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                    "Delete certificate successfully!",
                                    Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                                "Cannot delete certificate!",
                                Toast.LENGTH_SHORT).show());
                break;
        }
        return super.onContextItemSelected(item);
    }
}