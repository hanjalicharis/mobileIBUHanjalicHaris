//CREATED BY HANJALIC HARIS 2020

package com.example.projectmanhattan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.nio.channels.SelectableChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private String current_date, current_time, post_name_r;
    private String downloadURL;

    private ImageButton newPostImage;
    private Button newPostButton;
    private EditText PostDescription;

    private String message;
    private String currentUserID;

    private ProgressDialog loadingBar;
    final static int Gallery_Pick = 1;
    private Uri ImageUri;

    private StorageReference PostsImgRef;
    private FirebaseAuth mAuth;
    private DatabaseReference useresReference, postsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        PostsImgRef = FirebaseStorage.getInstance().getReference();

        useresReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postsReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);

        newPostImage = (ImageButton) findViewById(R.id.select_image);
        newPostButton = (Button) findViewById(R.id.new_post_button);
        PostDescription = (EditText) findViewById(R.id.post_description);

        mToolbar=(Toolbar) findViewById(R.id.post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create a new post!");

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToGallery();
            }
        });

        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ValidateInformation();
            }
        });

    }

    private void ValidateInformation()
    {
        message = PostDescription.getText().toString();
        if (ImageUri == null)
        {
            Toast.makeText(this, "Please select a photo first!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please tell us what do you want to do with stuff from the photo!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Adding your post!");
            loadingBar.setMessage("Please wait!");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoreImageToFB();
        }

    }

    private void StoreImageToFB()
    {
        Calendar calendarForDate = Calendar.getInstance();
        SimpleDateFormat current = new SimpleDateFormat("dd-MMMM-yyyy");
        current_date = current.format(calendarForDate.getTime());

        Calendar calendarForTime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm");
        current_time = currenttime.format(calendarForTime.getTime());

        // HALF RANDOM NAME FOR POSTS (addition of date and time)
        post_name_r = current_date + current_time;


        // MAKING OF A NEW FOLDER IN DATABASE
        // second .child() is the most important of all this code, it assigns specific name not to interfere with previous posts, will make it of real image name + date and time of the post
        StorageReference filePath =  PostsImgRef.child("Post Images").child(ImageUri.getLastPathSegment() + post_name_r + ".jpg");

        // SAVING PHOTO INTO FIREBASE
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    downloadURL = task.getResult().getStorage().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Image uploaded successfuly!", Toast.LENGTH_SHORT).show();
                    SavingPostInfo();

                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error! "+message, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    // SAVING POST INFORMATIONS INTO FIREBASE DATABASE

    private void SavingPostInfo()
    {
        useresReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String user_full_name = dataSnapshot.child("fullname").getValue().toString();
                    String user_profile_image = dataSnapshot.child("ProfileImage").getValue().toString();
                    String desc_of_a_post = PostDescription.getText().toString();

                    HashMap posts = new HashMap();
                    posts.put("UserID", currentUserID);
                    posts.put("Date", current_date);
                    posts.put("Time", current_time);
                    posts.put("Description", desc_of_a_post);
                    posts.put("ImgURL", downloadURL);
                    posts.put("ProfileImgURL", user_profile_image);
                    posts.put("FullName", user_full_name);

                    // GIVES UNIQUE ID TO A SPECIFIC USER POST
                    postsReference.child(currentUserID).updateChildren(posts).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if (task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "You have successfuly posted new post!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Gallery_Pick && resultCode == RESULT_OK && data != null)
        {
            ImageUri = data.getData();
            newPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id=item.getItemId();

        if (id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}