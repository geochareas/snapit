package com.geochareas.snapit;

import android.app.Activity;
import android.content.Intent;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.geochareas.snapit.Adapters.ProfileSnapAdapter;
import com.geochareas.snapit.Utility.Follow;
import com.geochareas.snapit.Utility.Snap;
import com.geochareas.snapit.Utility.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfile extends Activity {

    private long snapsCount;
    private long followersCount;
    private long followingCount;
    private boolean hasFollowed;
    private User mUser;
    private ArrayList<Snap> snaps = new ArrayList<>();


    private DatabaseReference mDatabase;
    private DatabaseReference followingRef;
    private FirebaseUser fbUser;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProfileSnapAdapter mAdapter;
    private CircleImageView mProfileImg;
    private TextView mUsername;
    private TextView mFollowers;
    private TextView mFollowing;
    private TextView mSnaps;
    private Button mFollowBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_profile);

        Intent i = getIntent();

        mUser = (User) i.getParcelableExtra("User");

        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mProfileImg = findViewById(R.id.profile_image);
        mUsername = findViewById(R.id.display_name);
        mSnaps = findViewById(R.id.snapsView);
        mFollowers = findViewById(R.id.followers);
        mFollowing = findViewById(R.id.following);
        mFollowBtn = findViewById(R.id.followBtn);

        recyclerView = findViewById(R.id.profileSnaps);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ProfileSnapAdapter(snaps, this);
        recyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        Picasso.get().load(mUser.photoUrl).into(mProfileImg);

        mUsername.setText(mUser.username);

        if (!fbUser.getUid().equals(mUser.uid)) { // case user viewing his profile

            mFollowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    update();
                }

            });

            hasFollowed = false;


            Query followingStatus = mDatabase.child("follows").orderByChild("follower").equalTo(fbUser.getUid());
            followingStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        String uid = d.child("followingUser").getValue(String.class);
                        if (mUser.uid.equals(uid)) {
                            hasFollowed = true;
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            if (hasFollowed) {
                // update drawable
                Drawable drawable = getResources().getDrawable(R.drawable.follow, getTheme());
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, getResources().getColor(R.color.colorAccent, getTheme()));
                mFollowBtn.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

                // update background
                drawable = getResources().getDrawable(R.drawable.transp_outlined, getTheme());
                mFollowBtn.setBackground(drawable);
            } else {
                // Update drawable
                Drawable drawable = getResources().getDrawable(R.drawable.follow, getTheme());
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, getResources().getColor(R.color.black, getTheme()));

                // update background
                mFollowBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent, getTheme()));
                mFollowBtn.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

            }
            mFollowBtn.setVisibility(View.VISIBLE);
        }

        Query snapsQuery = mDatabase.child("snaps").orderByChild("userId").equalTo(mUser.uid);
        snapsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                snapsCount = dataSnapshot.getChildrenCount();
                mSnaps.setText(Long.toString(snapsCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        snapsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                // A new snap has been added, add it to the displayed list
                final Snap snap = dataSnapshot.getValue(Snap.class);

                snap.user = mUser;
                mAdapter.notifyDataSetChanged();
                mAdapter.addSnap(snap);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Query followersQuery = mDatabase.child("follows").orderByChild("followingUser").equalTo(mUser.uid);
        followersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                followersCount = dataSnapshot.getChildrenCount();
                mFollowers.setText(Long.toString(followersCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query followingQuery = mDatabase.child("follows").orderByChild("follower").equalTo(mUser.uid);
        followingQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                followingCount = dataSnapshot.getChildrenCount();
                mFollowing.setText(Long.toString(followingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void update() {
        if (hasFollowed) {

            Query follow = mDatabase.child("follows").orderByChild("follower").equalTo(fbUser.getUid());

            follow.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshots) {

                    for (DataSnapshot d : dataSnapshots.getChildren()) {
                        String currentUid = d.child("followingUser").getValue(String.class);

                        if (currentUid.equals(mUser.uid)) {
                            followingRef = d.getRef();
                            break;
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if (followingRef != null) {
                followingRef.removeValue();
                int count = Integer.parseInt(mFollowers.getText().toString()) - 1;
                mFollowers.setText(Integer.toString(count));

                // Update drawable
                Drawable drawable = getResources().getDrawable(R.drawable.follow, getTheme());
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, getResources().getColor(R.color.black, getTheme()));

                // update background
                mFollowBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent, getTheme()));
                mFollowBtn.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

                hasFollowed = false;
                Toast.makeText(getApplicationContext(), "Unfollowed", Toast.LENGTH_SHORT).show();
            }

        } else {
            Follow follow = new Follow(mUser.uid, fbUser.getUid());
            String key = mDatabase.child("follows").push().getKey();
            mDatabase.child("follows").child(key).setValue(follow);
            int count = Integer.parseInt(mFollowers.getText().toString()) + 1;
            mFollowers.setText(Integer.toString(count));

            // update drawable
            Drawable drawable = getResources().getDrawable(R.drawable.follow, getTheme());
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, getResources().getColor(R.color.colorAccent));
            mFollowBtn.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

            // update background
            Drawable d = getResources().getDrawable(R.drawable.transp_outlined, getTheme());
            mFollowBtn.setBackground(d);

            hasFollowed = true;
            Toast.makeText(this, "Started following", Toast.LENGTH_SHORT).show();

        }

    }


}
