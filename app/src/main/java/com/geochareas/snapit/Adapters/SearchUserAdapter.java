package com.geochareas.snapit.Adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.geochareas.snapit.FeedActivity;
import com.geochareas.snapit.R;
import com.geochareas.snapit.SearchUser;
import com.geochareas.snapit.Utility.Snap;
import com.geochareas.snapit.Utility.User;
import com.geochareas.snapit.ViewProfile;
import com.geochareas.snapit.WatchSnap;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {
    private ArrayList<User> mDataset;
    private SearchUser mActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mPicture;
        public TextView mUsername;
        public RelativeLayout mUserRow;


        public ViewHolder(View v) {
            super(v);
            mPicture = v.findViewById(R.id.profile_photo);
            mUsername = v.findViewById(R.id.username);
            mUserRow = v.findViewById(R.id.snap_profile_row);
        }
    }

    public SearchUserAdapter(ArrayList<User> myDataset, SearchUser activity) {
        mDataset = myDataset;
        mActivity = activity;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public SearchUserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_user_list, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {


        final ViewHolder mHolder = holder;
        final User user = (User) mDataset.get(position);

        if (user != null) {


            Picasso.get().load(user.photoUrl).into(holder.mPicture, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    mHolder.mUsername.setText(user.username);

                }

                @Override
                public void onError(Exception e) {

                }

            });
        }

        holder.mUserRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(v.getContext(), ViewProfile.class);
                i.putExtra("User", user);
                v.getContext().startActivity(i);
                return false;
            }
        });

        holder.mUsername.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent i = new Intent(v.getContext(), ViewProfile.class);
                i.putExtra("User", user);
                v.getContext().startActivity(i);
                return false;
            }
        });


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addUser(User user) {
        mDataset.add(0, user);
        notifyDataSetChanged();
    }


}

