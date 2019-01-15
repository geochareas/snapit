package com.geochareas.snapit.Utility;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;



@IgnoreExtraProperties
public class User implements Parcelable {
    public String uid;
    public String fullname;
    public String username;
    public String photoUrl;
    public String token;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String fullName, String username, String token) {
        this.uid = uid;
        this.fullname = fullName;
        this.token = token;
        this.username = username;
    }

    public User(String uid, String fullName, String username, String imgSrc, String token) {
        this.uid = uid;
        this.fullname = fullName;
        this.username = username;
        this.photoUrl = imgSrc;
        this.token = token;
    }

    public User(Parcel in) {
        uid = in.readString();
        username = in.readString();
        photoUrl = in.readString();
        fullname = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.username);
        dest.writeString(this.photoUrl);
        dest.writeString(this.fullname);
    }

    // This is to de-serialize the object
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }


        @Override
        public User[] newArray(int size) {
            return new User[0];
        }
    };
}