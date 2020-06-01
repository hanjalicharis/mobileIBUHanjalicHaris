package com.example.projectmanhattan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button SearchButton;
    private EditText searchPeople;
    private RecyclerView search_results_list;
    private FirebaseAuth mAuth;
    private DatabaseReference allUsersDR;
    private Query postQuery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mToolbar = (Toolbar) findViewById(R.id.find_friends_app_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find people!");

        mAuth = FirebaseAuth.getInstance();

        // DEFINED ALLUSERSDR AND POSTQUERY TO SHOW ONLY SPECIFIC RESULTS (I WAS ASKING FOR). ACCORDING TO STACKOVERFLOW, THIS WORKS, BUT IN REAL LIFE SEEMS NOT, TRYING TO FIX IT :/
        allUsersDR = FirebaseDatabase.getInstance().getReference().child("Users");
        postQuery = allUsersDR.orderByChild("fullname");

        search_results_list = (RecyclerView) findViewById(R.id.search_result_list_of_people);
        search_results_list.setHasFixedSize(true);
        search_results_list.setLayoutManager(new LinearLayoutManager(this));


        SearchButton = (Button) findViewById(R.id.search_button);
        searchPeople = (EditText) findViewById(R.id.search_box);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchBoxInput = searchPeople.getText().toString();
                FindPeople(searchBoxInput);
            }
        });

    }

    private void FindPeople(String searchBoxInput)
    {
        Toast.makeText(this, "Searching!", Toast.LENGTH_LONG).show();
        Query search = allUsersDR.orderByChild("fullname").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery(postQuery, FindFriends.class).build();
        FirebaseRecyclerAdapter<FindFriends, FindFriendsActivity.FFViewHolder> adapter = new FirebaseRecyclerAdapter<FindFriends, FFViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FFViewHolder holder, int position, @NonNull FindFriends model)
            {
                holder.myName.setText(model.getFullname());
                holder.myStatus.setText(model.getStatus());
                Picasso.with(FindFriendsActivity.this).load(model.getProfileImage()).placeholder(R.drawable.profile).into(holder.myImage);
            }

            @NonNull
            @Override
            public FFViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display, parent, false);
                FFViewHolder FindFriendsVH = new FFViewHolder(view);
                return FindFriendsVH;
            }
        };
        search_results_list.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FFViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView myImage;
        TextView myName, myStatus;

        public FFViewHolder(@NonNull View itemView)
        {
            super(itemView);
            myImage = itemView.findViewById(R.id.all_users_profile_image);
            myName =  itemView.findViewById(R.id.all_users_full_name);
            myStatus = itemView.findViewById(R.id.all_users_status);
        }


    }
}
