package com.geochareas.snapit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;


public class WatchReply extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_reply);

        Intent i = getIntent();
        String photoUrl = i.getStringExtra("photoUrl");
        String name = i.getStringExtra("username");

        ImageView photo = findViewById(R.id.photo);
        TextView username = findViewById(R.id.reply_username);

        username.setText(name);
        Picasso.get().load(photoUrl).into(photo);
    }
}
