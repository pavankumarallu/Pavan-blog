package com.android.pavanblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private CircleImageView circleimage;
    private Uri mainImageURI = null;
    private EditText setupName;
    private Button setup_btn;
    private String user_id;
    private Boolean isChanged=false;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressbar;
    private FirebaseFirestore firebaseFirestore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Toolbar toolbar = findViewById(R.id.toolbar_Setup);



        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Settings");

        circleimage = findViewById(R.id.setupImage);
        setupName = findViewById(R.id.setupname);
        setup_btn = findViewById(R.id.setupbtn);
        progressbar = findViewById(R.id.setup_pb);



        firebaseAuth = FirebaseAuth.getInstance();

        user_id = firebaseAuth.getCurrentUser().getUid();


        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressbar.setVisibility(View.VISIBLE);
        setup_btn.setEnabled(false);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){
                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageURI = Uri.parse(image);
                        setupName.setText(name);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.profile);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(circleimage);


                    }

                }
                else{

                    String e=task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Error : "+e, Toast.LENGTH_SHORT).show();

                }
                progressbar.setVisibility(View.INVISIBLE);
                setup_btn.setEnabled(true);

            }
        });

        circleimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);



                    }
                    else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetupActivity.this);
                    }

                }
            }
        });

        setup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String username = setupName.getText().toString();
                if(isChanged) {
                    if (!TextUtils.isEmpty(username) && mainImageURI != null) {

                        user_id = firebaseAuth.getCurrentUser().getUid();
                        progressbar.setVisibility(View.VISIBLE);

                        final StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                        Task<Uri> urlTask = image_path.putFile(mainImageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return image_path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){
                                    storeFirestore(task,username);
                                }

                            }
                        });


                    }
                }else {
                    storeFirestore(null,username);
                }

                }
        });



    }

    private void storeFirestore(@NonNull Task<Uri> task,String username) {
        String download_uri;


        if (task!=null) {
            download_uri = task.getResult().toString();
        }
        else{
            download_uri = mainImageURI.toString();
        }
        Map<String,String> userMap = new HashMap<>();
        userMap.put("name",username);
        userMap.put("image",download_uri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    Toast.makeText(SetupActivity.this, "Updated the Account settings", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SetupActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();

                }else{
                    String e=task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Error : "+e, Toast.LENGTH_SHORT).show();
                }
                progressbar.setVisibility(View.INVISIBLE);

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();
                circleimage.setImageURI(mainImageURI);
                isChanged=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}