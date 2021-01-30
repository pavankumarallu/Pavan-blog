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

public class LoginActivity extends AppCompatActivity {


    private EditText LoginEmailText;
    private EditText LoginPassText;
    private Button loginBtn;
    private Button loginRegBtn;
    private FirebaseAuth mAuth;
    private ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        loginBtn = (Button) findViewById(R.id.Register_btn);
        loginRegBtn = (Button) findViewById(R.id.reg_log_btn);
        LoginEmailText = (EditText) findViewById(R.id.RegEmail);
        LoginPassText = (EditText) findViewById(R.id.RegPassword);
        loginProgress = (ProgressBar) findViewById(R.id.reg_pb);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginEmail = LoginEmailText.getText().toString();
                String loginPass = LoginPassText.getText().toString();
                if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)){

                    loginProgress.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(loginEmail,loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }
                            else{
                                String e = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error : "+e,Toast.LENGTH_SHORT).show();
                            }
                            loginProgress.setVisibility(View.INVISIBLE);

                        }
                    });

                }



            }
        });
        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,CreateAccountActivity.class);
                startActivity(intent);
                finish();
            }
        });

        
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(mainIntent);
            finish();
        }


    }
}