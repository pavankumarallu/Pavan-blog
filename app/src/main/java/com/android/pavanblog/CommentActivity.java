package com.android.pavanblog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

public class CommentActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText comments;
    private ImageView send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        toolbar = findViewById(R.id.post_tool);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");


        comments = findViewById(R.id.Comments_Secton);
        send = findViewById(R.id.send);

    }
}