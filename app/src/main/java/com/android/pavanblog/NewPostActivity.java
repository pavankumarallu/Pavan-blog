package com.android.pavanblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENHTH = 100;
    private Toolbar newposttoolbar;
    private ImageView image;
    private EditText Description;
    private Button post_btn;
    private Uri PostImageUri = null;
    private ProgressBar progress;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        newposttoolbar = findViewById(R.id.post_tool);
        image = findViewById(R.id.Post_Image);
        Description = findViewById(R.id.DescriptionPost);
        post_btn = findViewById(R.id.postBtn);
        progress = findViewById(R.id.new_post_pb);
        firebaseAuth = FirebaseAuth.getInstance();

        setSupportActionBar(newposttoolbar);
        getSupportActionBar().setTitle("Add new Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);

            }
        });

        post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc = Description.getText().toString();
                if (!TextUtils.isEmpty(desc) && PostImageUri!=null){
                    progress.setVisibility(View.VISIBLE);
                    String randomName = randomString();
                    final StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    Task<Uri> urlTask = filePath.putFile(PostImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String downloadUri = task.getResult().toString();
                                current_user_id = firebaseAuth.getCurrentUser().getUid();

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("image_url", downloadUri);
                                postMap.put("desc", desc);

                                postMap.put("user_id", current_user_id);
                                postMap.put("timestamp", FieldValue.serverTimestamp());

                                firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(NewPostActivity.this, "Post was Added", Toast.LENGTH_LONG).show();
                                            Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                            startActivity(mainIntent);
                                            finish();

                                        } else {
                                            String tempMessage = task.getException().getMessage();
                                            Toast.makeText(NewPostActivity.this, "Error: " + tempMessage, Toast.LENGTH_LONG).show();

                                        }
                                        progress.setVisibility(View.INVISIBLE);
                                    }
                                });



                            } else {
                                progress.setVisibility(View.INVISIBLE);
                                String tempMessage = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error: " + tempMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                PostImageUri = result.getUri();
                image.setImageURI(PostImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    public static String randomString(){
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENHTH);
        char tempChar;
        for (int i = 0;i<randomLength;i++){
            tempChar = (char)(generator.nextInt(96)+32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}