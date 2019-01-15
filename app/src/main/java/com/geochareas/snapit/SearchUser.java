package com.geochareas.snapit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.geochareas.snapit.Adapters.SearchUserAdapter;
import com.geochareas.snapit.Adapters.SnapAdapter;
import com.geochareas.snapit.Utility.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchUser extends Activity {

    private EditText mSearchField;
    private Button mSearchBtn;
    private RecyclerView mResults;

    private DatabaseReference mDatabase;
    private ArrayList<User> users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_user);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");


        mSearchField = findViewById(R.id.search_field);
        mSearchBtn = findViewById(R.id.search_btn);

        mResults = findViewById(R.id.result_list);
        mResults.setLayoutManager(new LinearLayoutManager(this));

        // insert a divider between recyclerview items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mResults.getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.divider));
        mResults.addItemDecoration(dividerItemDecoration);




        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable edit) {
                if (edit.length() != 0) {
                    String username = mSearchField.getText().toString();
                    searchUser(username);
                }
            }
        });


    }

    public void searchUser(String searchText) {


        Query searchUserQuery = mDatabase.orderByChild("username").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerAdapter<User, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.search_user_list,
                UserViewHolder.class,
                searchUserQuery
        ) {

            @Override
            public void populateViewHolder(UserViewHolder viewHolder, User user, int position) {
                viewHolder.setDetails(user);

            }


        };

        mResults.setAdapter(firebaseRecyclerAdapter);

    }


    // View Holder Class

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setDetails(final User user) {

            TextView user_name = mView.findViewById(R.id.username);
            ImageView user_photo = mView.findViewById(R.id.profile_photo);
            RelativeLayout row = mView.findViewById(R.id.snap_profile_row);

            user_name.setText(user.fullname);
            Picasso.get().load(user.photoUrl).into(user_photo);

            row.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Intent i = new Intent(v.getContext(), ViewProfile.class);
                    i.putExtra("User", user);
                    v.getContext().startActivity(i);
                    return false;
                }
            });




        }


    }

}
