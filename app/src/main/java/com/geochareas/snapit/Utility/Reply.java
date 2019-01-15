package com.geochareas.snapit.Utility;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Reply {
    public String username;
    public String postId;
    public String userId;
    public String downloadUrl;


    public Reply() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Reply(String username, String postId, String userId, String downloadUrl) {
        this.username = username;
        this.postId = postId;
        this.userId = userId;
        this.downloadUrl = downloadUrl;

    }

    public Reply(String username, String downloadUrl) {
        this.username = username;
        this.downloadUrl = downloadUrl;

    }

}
