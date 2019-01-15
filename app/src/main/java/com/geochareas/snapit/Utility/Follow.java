package com.geochareas.snapit.Utility;

import com.google.firebase.database.IgnoreExtraProperties;


public class Follow {
    public String followingUser;
    public String follower;

    public Follow() {
        // Default constructor required for calls
    }

    public Follow(String followingUser, String follower) {
        this.followingUser = followingUser;
        this.follower = follower;
    }
}
