package com.example.projectmanhattan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView profile_name,profile_username, profile_status, profile_country, profile_dob, profile_gender;
    private CircleImageView profile_image;
    private DatabaseReference profileUsersRef;
    private FirebaseAuth mAuth;
    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth=FirebaseAuth.getInstance();
        current_user_id= mAuth.getCurrentUser().getUid();
        profileUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

        profile_name=(TextView) findViewById(R.id.profile_profile_name);
        profile_username=(TextView) findViewById(R.id.profile_profile_username);
        profile_status=(TextView) findViewById(R.id.profile_profile_status);
        profile_country = (TextView) findViewById(R.id.profile_profile_country);
        profile_dob=(TextView) findViewById(R.id.profile_profile_dob);
        profile_gender=(TextView) findViewById(R.id.profile_profile_gender);

        profile_image = (CircleImageView) findViewById(R.id.profile_profile_image);

        profileUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String pProfileIMG = Objects.requireNonNull(dataSnapshot.child("ProfileImage").getValue()).toString();
                    String pDoB = Objects.requireNonNull(dataSnapshot.child("DateOfBirth").getValue()).toString();
                    String pUsername = Objects.requireNonNull(dataSnapshot.child("username").getValue()).toString();
                    String pUserFullName = Objects.requireNonNull(dataSnapshot.child("fullname").getValue()).toString();
                    String pCountry = Objects.requireNonNull(dataSnapshot.child("country").getValue()).toString();
                    String pStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                    String pGender = Objects.requireNonNull(dataSnapshot.child("gender").getValue()).toString();

                    Picasso.with(ProfileActivity.this).load(pProfileIMG).placeholder(R.drawable.profile).into(profile_image);
                    profile_username.setText(pUsername);
                    profile_status.setText(pStatus);
                    profile_name.setText(pUserFullName);
                    profile_country.setText( pCountry);
                    profile_dob.setText(pDoB);
                    profile_gender.setText(pGender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }
}