package com.android.pavanblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText regEmail;
    private EditText regPass;
    private EditText regConfPass;
    private Button RegBtn;
    private Button LogRegBtn;
    private ProgressBar reg_pro;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        regEmail = (EditText)findViewById(R.id.RegEmail);
        regPass = (EditText)findViewById(R.id.RegPassword);
        regConfPass = (EditText)findViewById(R.id.RegConfirmPassword);
        RegBtn = (Button)findViewById(R.id.Register_btn);
        LogRegBtn = (Button)findViewById(R.id.reg_log_btn);
        reg_pro = (ProgressBar)findViewById(R.id.reg_pb);

        RegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = regEmail.getText().toString();
                String password = regPass.getText().toString();
                String conf_Pass = regConfPass.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(conf_Pass));
                {

                    if (password.equals(conf_Pass)){
                        reg_pro.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    Intent mainIntent = new Intent(CreateAccountActivity.this,SetupActivity.class);
                                    startActivity(mainIntent);
                                    finish();

                                }else{
                                    String e = task.getException().getMessage();
                                    Toast.makeText(CreateAccountActivity.this, "Error : "+e, Toast.LENGTH_SHORT).show();
                                }

                                reg_pro.setVisibility(View.INVISIBLE);
                            }

                        });
                        
                    }
                    else {
                        Toast.makeText(CreateAccountActivity.this, "Password Doesn't Match", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
        LogRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(CreateAccountActivity.this,LoginActivity.class);
                startActivity(mainIntent);

                finish();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            Intent mainIntent = new Intent(CreateAccountActivity.this,MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }
}