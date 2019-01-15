package com.geochareas.snapit;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.geochareas.snapit.Adapters.ReplyAdapter;
import com.geochareas.snapit.Utility.Reply;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class ViewReplies extends Activity {

    private DatabaseReference database;
    private String mSnapId;
    private ReplyAdapter mAdapter;
    private GridView mGridView;
    private ImageView mSnapFullscreen;
    private ImageView mReplyPhoto;
    private ArrayList<Reply> mReplies = new ArrayList<>();
    private ShimmerFrameLayout container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_replies);

        Intent i = getIntent();
        mSnapId = i.getStringExtra("snapId");

        database = FirebaseDatabase.getInstance().getReference();

        container = findViewById(R.id.replies_shimmer);
        container.startShimmer();
        container.setVisibility(View.VISIBLE);

        mGridView = findViewById(R.id.reply_grid);


        //ADAPTER
        mAdapter = new ReplyAdapter(mReplies, this);
        mGridView.setAdapter(mAdapter);

        int width = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = width/3;
        mGridView.setColumnWidth(width);


        Query repliesQuery = database.child("replies").child(mSnapId);
        repliesQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    final Reply reply = dataSnapshot.getValue(Reply.class);
                    mAdapter.addReply(reply);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        container.setVisibility(View.GONE);
        mGridView.setVisibility(View.VISIBLE);
    }


}
