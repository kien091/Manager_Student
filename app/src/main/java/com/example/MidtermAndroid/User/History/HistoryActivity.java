package com.example.MidtermAndroid.User.History;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.MidtermAndroid.R;
import com.example.MidtermAndroid.User.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class HistoryActivity extends AppCompatActivity {
    FirebaseFirestore database;
    ListView lv_history;
    ArrayAdapter<String> historyAdapter;
    ArrayList<String> historyList;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        database = FirebaseFirestore.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lv_history = findViewById(R.id.lv_history);
        historyList = new ArrayList<>();
        historyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        lv_history.setAdapter(historyAdapter);

        Intent intent = getIntent();
        if(intent.hasExtra("user")){
            user = (User) intent.getSerializableExtra("user");
            getSupportActionBar().setTitle(user.getName() + "'s History");
            loadHistoryFromFirestore(user);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadHistoryFromFirestore(User user) {
        ArrayList<Date> sortedDates = new ArrayList<>();

        historyList.clear();
        database.collection("users")
                .document(user.getUid())
                .collection("history")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                            Date history = snapshot.getTimestamp("timestamp").toDate();
                            sortedDates.add(history);
                        }

                        Collections.sort(sortedDates, Collections.reverseOrder());

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        for (Date date : sortedDates) {
                            String historyString = sdf.format(date);
                            historyList.add(historyString);
                        }

                        historyAdapter.notifyDataSetChanged();
                    }
                });
    }
}