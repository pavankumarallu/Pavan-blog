package com.android.pavanblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Toolbar main_tool;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId;
    private FloatingActionButton floatingActionButton;
    private BottomNavigationView mainNav;
    private HomeFragment homeFragment;
    private AccountFragment accountFragment;
    private NotificationFragment notificationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();




        main_tool = (Toolbar) findViewById(R.id.main_tool);
        mainNav = findViewById(R.id.mainNavBar);
        setSupportActionBar(main_tool);
        getSupportActionBar().setTitle("Pavan's Blog");

        if (mAuth.getCurrentUser()!=null) {

            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();

            replaceFragment(homeFragment);

            mainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.home:
                            replaceFragment(homeFragment);
                            return true;
                        case R.id.Account:
                            replaceFragment(accountFragment);
                            return true;
                        case R.id.notification:
                            replaceFragment(notificationFragment);
                            return true;
                        default:
                            return false;
                    }


                }
            });

            floatingActionButton = findViewById(R.id.add_post);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(intent);

                }
            });
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){

            Intent LoginIntent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(LoginIntent);
            finish();

        }else{
            currentUserId = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()){
                        if (!task.getResult().exists()){
                            Intent SetupIntent = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(SetupIntent);
                            finish();
                        }
                    }else
                    {
                        String e = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : "+e, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case  R.id.action_logout_btn:
                logOut();
                return true;

            case R.id.settings_btn:
                Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(intent);

            default:
                return false;

        }



    }

    private void logOut() {
        mAuth.signOut();
        Intent LoginIntent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(LoginIntent);
        finish();
    }

    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();

    }
}