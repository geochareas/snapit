package com.geochareas.snapit.Utility;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Snap implements Parcelable {
    public String key;
    public String userId;
    public String location;
    public String downloadUrl;
    public String date;



    // these properties will not be saved to the database
    @Exclude
    public User user;


    public Snap() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Snap(String key, String userId, String location, String date, String downloadUrl) {
        this.key = key;
        this.userId = userId;
        this.location = location;
        this.date = date;
        this.downloadUrl = downloadUrl;
    }

    public Snap(Parcel in) {
        key = in.readString();
        userId = in.readString();
        location = in.readString();
        date = in.readString();
        downloadUrl = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeString(this.userId);
        dest.writeString(this.location);
        dest.writeString(this.date);
        dest.writeString(this.downloadUrl);

    }
    // This is to de-serialize the object
    public static final Parcelable.Creator<Snap> CREATOR = new Parcelable.Creator<Snap>() {
        public Snap createFromParcel(Parcel in) {
            return new Snap(in);
        }


        @Override
        public Snap[] newArray(int size) {
            return new Snap[0];
        }
    };
}
