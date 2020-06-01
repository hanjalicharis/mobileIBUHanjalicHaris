package com.example.projectmanhattan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {

    private String current_user_id;
    final static int Gallery_Pick = 1;
    private Toolbar mToolbar;
    private EditText status,username,userfullname,country,dob,gender;
    private Button updateSettings;
    private CircleImageView userProfileIMG;
    private DatabaseReference SettingsRef;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        SettingsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("User Profile Images");

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        status = (EditText) findViewById(R.id.settings_status);
        username = (EditText) findViewById(R.id.settings_username);
        userfullname = (EditText) findViewById(R.id.settings_userfullname);
        country = (EditText) findViewById(R.id.settings_country);
        dob = (EditText) findViewById(R.id.settings_dateofbirth);
        gender = (EditText) findViewById(R.id.settings_gender);
        updateSettings = (Button) findViewById(R.id.update_account_settings);
        userProfileIMG = (CircleImageView) findViewById(R.id.settings_profile_image);

        SettingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String sProfileIMG = dataSnapshot.child("ProfileImage").getValue().toString();
                    String sDoB = dataSnapshot.child("DateOfBirth").getValue().toString();
                    String sUsername = dataSnapshot.child("username").getValue().toString();
                    String sUserFullName = dataSnapshot.child("fullname").getValue().toString();
                    String sCountry = dataSnapshot.child("country").getValue().toString();
                    String sStatus = dataSnapshot.child("status").getValue().toString();
                    String sGender = dataSnapshot.child("gender").getValue().toString();

                    Picasso.with(SettingsActivity.this).load(sProfileIMG).placeholder(R.drawable.profile).into(userProfileIMG);
                    username.setText(sUsername);
                    status.setText(sStatus);
                    userfullname.setText(sUserFullName);
                    country.setText(sCountry);
                    dob.setText(sDoB);
                    gender.setText(sGender);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AccountValidationInfo();
            }
        });

        userProfileIMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null)
        {
            Uri ImageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Updating your account!");
                loadingBar.setMessage("Please wait!");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                final StorageReference imagePath = UserProfileImageRef.child(current_user_id + "jpg");

                imagePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Intent selfIntent = new Intent(SettingsActivity.this, SetupActivity.class);
                            startActivity(selfIntent);
                            Toast.makeText(SettingsActivity.this, "Profile image cropped and saved successfuly!", Toast.LENGTH_SHORT).show();
                            final String imageUrl = task.getResult().getStorage().getDownloadUrl().toString();
                            SettingsRef.child("ProfileImage").setValue(imageUrl);
                            loadingBar.dismiss();
                        }
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
                loadingBar.dismiss();
            }
        }

    }
    private void AccountValidationInfo()
    {
        String Ausername = username.getText().toString();
        String Auserfullname = userfullname.getText().toString();
        String Astatus = status.getText().toString();
        String Acountry = country.getText().toString();
        String Adob = dob.getText().toString();
        String Agender = gender.getText().toString();

        if (TextUtils.isEmpty(Ausername) || TextUtils.isEmpty(Auserfullname) || TextUtils.isEmpty(Astatus) || TextUtils.isEmpty(Acountry) || TextUtils.isEmpty(Adob) || TextUtils.isEmpty(Agender) )
        {
            Toast.makeText(this, "Please fill in all the fields!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            UpdateAccountInformation(Ausername, Auserfullname, Astatus, Acountry, Adob, Agender);
        }

    }

    private void UpdateAccountInformation(String ausername, String auserfullname, String astatus, String acountry, String adob, String agender)
    {
        HashMap usersMap = new HashMap();
        usersMap.put("username",ausername);
        usersMap.put("fullname",auserfullname);
        usersMap.put("DateOfBirth",adob);
        usersMap.put("country",acountry);
        usersMap.put("status",astatus);
        usersMap.put("gender",agender);

        SettingsRef.updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if (task.isSuccessful())
                {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Your information has been updated!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "Error occured!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}