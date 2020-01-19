package com.example.saludable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private ActionBarDrawerToggle actionBarDrawerToggle;
    private CircleImageView NavProfileImage;
    private TextView NavProfileusername;
    private ImageButton AddNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostRef;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );

        mAuth = FirebaseAuth.getInstance ();

        UsersRef = FirebaseDatabase.getInstance ().getReference ().child ( "Users" );
        PostRef = FirebaseDatabase.getInstance ().getReference ().child ( "Posts" );


        mToolbar = findViewById ( R.id.main_page_toolbar );
        setSupportActionBar ( mToolbar );
        getSupportActionBar ().setTitle ( "Home" );

        AddNewPostButton = findViewById ( R.id.add_new_post_button );


        drawerLayout = findViewById ( R.id.drawable_layout );
        actionBarDrawerToggle = new ActionBarDrawerToggle ( MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_open );
        drawerLayout.addDrawerListener ( actionBarDrawerToggle );
        actionBarDrawerToggle.syncState ();
        getSupportActionBar ().setDisplayHomeAsUpEnabled ( true );
        navigationView = findViewById ( R.id.navigation_view );

        postList = findViewById ( R.id.all_users_post_list );
        postList.setHasFixedSize ( true );

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager ( this );
        linearLayoutManager.setReverseLayout ( true );
        linearLayoutManager.setStackFromEnd ( true );

        postList.setLayoutManager ( linearLayoutManager );


        View navView = navigationView.inflateHeaderView ( R.layout.navigation_header );
        NavProfileImage = navView.findViewById ( R.id.nav_profile_image );
        NavProfileusername = navView.findViewById ( R.id.nav_user_full_name );

        navigationView.setNavigationItemSelectedListener ( new NavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector ( item );
                return false;
            }
        } );
    }


    @Override
    protected void onStart() {
        super.onStart ();

        FirebaseUser currentUser = mAuth.getCurrentUser ();

        if (currentUser == null) {
            SendUserTologinActivity ();
        } else {
            CheckUserExistence ();
            firebaseRecyclerAdapter.startListening ();
        }
    }


    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser ().getUid ();

        UsersRef.addValueEventListener ( new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild ( current_user_id )) {
                    SendUserToSetupActivity ();
                } else {
                    if (!dataSnapshot.child ( current_user_id ).hasChild ( "username" )) {
                        SendUserToSetupActivity ();
                    } else {
                        if (dataSnapshot.child ( current_user_id ).hasChild ( "fullname" )) {
                            String fullname = dataSnapshot.child ( current_user_id ).child ( "fullname" ).getValue ().toString ();
                            NavProfileusername.setText ( fullname );

                        }
                        if (dataSnapshot.child ( current_user_id ).hasChild ( "profileimage" )) {
                            String image = dataSnapshot.child ( current_user_id ).child ( "profileimage" ).getValue ().toString ();

                            Picasso.with ( MainActivity.this ).load ( image ).placeholder ( R.drawable.profile ).into ( NavProfileImage );
                        } else {
                            Toast.makeText ( MainActivity.this, "El usuario no tiene foto de perfil", Toast.LENGTH_SHORT ).show ();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

        AddNewPostButton.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                SendUserToPostActivity ();
            }
        } );


        DisplayAllUsersPosts ();
    }


    protected void DisplayAllUsersPosts() {


        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post> ()
                .setQuery ( PostRef, Post.class ).build ();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, PostViewHolder> ( options ) {
                    @Override
                    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from ( parent.getContext () )
                                .inflate ( R.layout.all_post_layout, parent, false );
                        return new PostViewHolder ( view );
                    }

                    @Override
                    protected void onBindViewHolder(PostViewHolder postViewHolder, int i, @NonNull Post post) {
                        postViewHolder.setFullname ( post.getFullname () );
                        postViewHolder.setTime ( post.getTime () );
                        postViewHolder.setDate ( post.getDate () );
                        postViewHolder.setDescription ( post.getDescription () );
                        postViewHolder.setProfileimage ( getApplicationContext (), post.getProfileimage () );
                        postViewHolder.setPostImage ( getApplicationContext (), post.getPostimage () );

                    }

                };

        postList.setAdapter ( firebaseRecyclerAdapter );


    }

    private void SendUserToPostActivity() {

        Intent addNewPostIntent = new Intent ( MainActivity.this, PostActivity.class );
        startActivity ( addNewPostIntent );
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId ()) {
            case R.id.nav_post:
                SendUserToPostActivity ();
                break;
            case R.id.nav_profile:
                Toast.makeText ( this, "Profile", Toast.LENGTH_SHORT ).show ();
                break;
            case R.id.nav_home:
                Toast.makeText ( this, "Home", Toast.LENGTH_SHORT ).show ();
                break;
            case R.id.nav_marathon:
                Toast.makeText ( this, "Marathon", Toast.LENGTH_SHORT ).show ();
                break;
            case R.id.nav_find_marathon:
                Toast.makeText ( this, "Find Marathon", Toast.LENGTH_SHORT ).show ();
                break;
            case R.id.nav_message:
                Toast.makeText ( this, "Messages", Toast.LENGTH_SHORT ).show ();
                break;
            case R.id.nav_settings:
                Toast.makeText ( this, "Settings", Toast.LENGTH_SHORT ).show ();
                break;
            case R.id.nav_Logout:
                mAuth.signOut ();
                SendUserTologinActivity ();
                break;

        }

    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent ( MainActivity.this, SetupActivity.class );
        setupIntent.addFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity ( setupIntent );
        finish ();
    }


    private void SendUserTologinActivity() {
        Intent loginIntent = new Intent ( MainActivity.this, LoginActivity.class );
        loginIntent.addFlags ( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity ( loginIntent );
        finish ();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected ( item )) {
            return true;
        }
        return super.onOptionsItemSelected ( item );
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Context mContext;

        public PostViewHolder(View itemView) {
            super ( itemView );
            mView = itemView;
            mContext = itemView.getContext ();
        }

        public void setFullname(String fullname) {
            TextView username = mView.findViewById ( R.id.post_profile_name );
            username.setText ( fullname );
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView image = mView.findViewById ( R.id.post_profile_image );
            Picasso.with ( ctx ).load ( profileimage ).into ( image );

        }

        public void setTime(String time) {
            TextView postTime = mView.findViewById ( R.id.post_time );
            postTime.setText ( time );
        }

        public void setDate(String date) {
            TextView postDate = mView.findViewById ( R.id.post_date );
            postDate.setText ( date );
        }

        public void setDescription(String description) {
            TextView postDescription = mView.findViewById ( R.id.post_description );
            postDescription.setText ( description );
        }

        public void setPostImage(Context ctx, String postImage) {
            ImageView image = mView.findViewById ( R.id.post_image );
            Picasso.with ( ctx ).load ( postImage ).into ( image );
        }

    }
}
