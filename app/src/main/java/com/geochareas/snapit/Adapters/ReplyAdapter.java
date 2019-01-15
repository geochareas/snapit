package com.geochareas.snapit.Adapters;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.geochareas.snapit.FeedActivity;
import com.geochareas.snapit.R;
import com.geochareas.snapit.Utility.SquareImageView;
import com.geochareas.snapit.Utility.Reply;
import com.geochareas.snapit.ViewReplies;
import com.geochareas.snapit.WatchReply;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

public class ReplyAdapter extends BaseAdapter {
    private ViewReplies mActivity;
    private ArrayList<Reply> mReplies = new ArrayList<Reply>();
    private ImageView mSnapFull;

    public ReplyAdapter(ArrayList<Reply> replies, ViewReplies activity) {
        this.mActivity = activity;
        this.mReplies = replies;
    }

    @Override
    public int getCount() {
        return mReplies.size();
    }


    public void addReply(Reply reply) {
        mReplies.add(0, reply);
        notifyDataSetChanged();
    }

    @Override
    public Reply getItem(int position) {
        return mReplies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_model, parent, false);
        }

        final SquareImageView replyPhoto = convertView.findViewById(R.id.reply_photo);

        final TextView username = convertView.findViewById(R.id.reply_username);

        final Reply r = this.getItem(position);


        Picasso.get().load(r.downloadUrl).into(replyPhoto, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                username.setText(r.username);
            }

            @Override
            public void onError(Exception e) {

            }

        });


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(mActivity, WatchReply.class);
                i.putExtra("username", r.username);
                i.putExtra("photoUrl", r.downloadUrl);
                mActivity.startActivity(i);


            }
        });

        return convertView;
    }

}