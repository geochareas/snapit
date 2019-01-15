package com.geochareas.snapit.Utility;

import android.util.Log;

import com.geochareas.snapit.Utility.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.firebase.ui.auth.AuthUI.TAG;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    // [START refresh_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        String token =  FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]


    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            User user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getDisplayName().toLowerCase(), firebaseUser.getPhotoUrl().toString(), token);
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child("users").child(user.uid).setValue(user);
        }
    }

}
