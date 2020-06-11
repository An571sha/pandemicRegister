package com.animesh.notfallapp.utility;

import android.util.Log;

import com.animesh.notfallapp.commons.User;
import com.google.firebase.database.DatabaseReference;

public class Utility {

    private static final String TAG = "SIGNUP_FIREBASE";


    public static void writeNewUser(DatabaseReference databaseReference, String email, String userId, String name, String phoneNumber){
        try {
            User user = new User(email, userId, name, phoneNumber);
            databaseReference.child("users").child(userId).setValue(user);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }


    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    public static String getTAG() {
        return TAG;
    }



}
