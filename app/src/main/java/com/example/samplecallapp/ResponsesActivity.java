package com.example.samplecallapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ResponsesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<UssdResponse> responseList;

    UssdAdapter adapter;
    DBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_responses);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.myRecycler);
        responseList = new ArrayList<>();
        adapter = new UssdAdapter(this);
        helper = new DBHelper(this);

        showData();
    }

    public List<UssdResponse> getResponses(){
        Cursor cursor = helper.getResponses();
        List<UssdResponse> myList = new ArrayList<>();
        if (cursor.getCount() == 0){
            Toast.makeText(this, "responses database empty", Toast.LENGTH_SHORT).show();
        }
        else{
            while (cursor.moveToNext()){
                String response = cursor.getString(cursor.getColumnIndexOrThrow("response"));
                String ussdCode = cursor.getString(cursor.getColumnIndexOrThrow("ussdCode"));
                int subscriptionId = cursor.getInt(cursor.getColumnIndexOrThrow("subscriptionId"));
                myList.add(new UssdResponse(response,ussdCode,subscriptionId));
            }
        }
        cursor.close();
        return myList;
    }

    public void showData(){
        adapter = new UssdAdapter(this);
        adapter.setResponseList(getResponses());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }
}