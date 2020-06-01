package com.example.projectmanhattan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postsList;
    private FirebaseRecyclerAdapter adapter;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageButton addnewpostbutton;

    private CircleImageView NavigationProfileImage;
    private TextView NavigationUsername;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private DatabaseReference PostsRef;
    private Query query;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        query=FirebaseDatabase.getInstance().getReference().child("Posts");


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        addnewpostbutton = (ImageButton) findViewById(R.id.add_new_post_button);

        drawerLayout=(DrawerLayout) findViewById(R.id.drawable_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView=(NavigationView) findViewById(R.id.navigation_view);

        postsList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postsList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        postsList.setLayoutManager(llm);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavigationProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavigationUsername = (TextView) navView.findViewById(R.id.nav_user_full_name);

        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavigationUsername.setText(fullname);
                    }
                   if (dataSnapshot.hasChild("ProfileImage"))
                   {
                       String image = dataSnapshot.child("ProfileImage").getValue().toString();
                       Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavigationProfileImage);
                   }
                   else
                   {
                       Toast.makeText(MainActivity.this, "You haven't set your full name!", Toast.LENGTH_SHORT).show();
                   }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });

        addnewpostbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToPostActivity();
            }
        });

    }


    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(PostsRef, Posts.class).build();

        FirebaseRecyclerAdapter<Posts, PostsVH> adapter = new FirebaseRecyclerAdapter<Posts, PostsVH>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsVH holder, int position, @NonNull Posts model)
            {

                holder.FullName.setText(model.getFullName());
                holder.Date.setText(model.getDate());
                holder.Time.setText(model.getTime());
                holder.Desc.setText(model.getDescription());
                Picasso.with(MainActivity.this).load(model.getProfileImgURL()).placeholder(R.drawable.profile).into(holder.ProfileImgURL);
                Picasso.with(MainActivity.this).load(model.getImgURL()).placeholder(R.drawable.profile).into(holder.ImgURL);
            }

            @NonNull
            @Override
            public PostsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.posts_layout, parent, false);
                PostsVH postsHolder = new PostsVH(view);
                return postsHolder;
            }
        };

        postsList.setAdapter(adapter);

        adapter.startListening();

        // CHECKS IF CURRENT USER IS SIGNED IN
        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            CheckUserExistence();
        }
    }




    private void SendUserToPostActivity()
    {
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        postIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(postIntent);
    }

    public static class PostsVH extends RecyclerView.ViewHolder
    {

         CircleImageView ProfileImgURL;
         TextView FullName, Date, Time, Desc;
        ImageView ImgURL;

        public PostsVH(@NonNull View itemView) {
            super(itemView);

            FullName = itemView.findViewById(R.id.post_profile_username);
            Date = itemView.findViewById(R.id.posting_date);
            Time = itemView.findViewById(R.id.posting_time);
            Desc = itemView.findViewById(R.id.post_text);
            ImgURL = itemView.findViewById(R.id.post_image);
            ProfileImgURL = itemView.findViewById(R.id.post_profile_img);


        }
    }


    private void CheckUserExistence()
    {
        final String current_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.hasChild(current_user_id))
                {
                    SendUserToSetupActivity();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToAboutActivity()
    {
        Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
        //aboutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(aboutIntent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }

    private void SendUserToFindPeopleActivity()
    {
        Intent findPeople = new Intent (MainActivity.this, FindFriendsActivity.class);
        startActivity(findPeople);
        finish();
    }

    private void SendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(profileIntent);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_post:
                SendUserToPostActivity();
            case R.id.nav_people:
                Toast.makeText(this, "People", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_people:
                SendUserToFindPeopleActivity();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
            case R.id.nav_about:
                SendUserToAboutActivity();
                break;
        }
    }



}
