package com.animesh.notfallapp.utility;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.animesh.notfallapp.commons.User;
import com.animesh.notfallapp.commons.UserLocationAndStatus;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

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

    public static void writeNewUserLocationAndStatus(DatabaseReference databaseReference, String userId, Double latitiude, Double longitude, String address, String status, String phoneNumber){
        try {
            UserLocationAndStatus user = new UserLocationAndStatus(userId,latitiude,longitude,address,status, phoneNumber);
            databaseReference.child("status").child(userId).setValue(user);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void showFirebaseExceptionToast(Context context, Task task){
        String exception = Objects.requireNonNull(task.getException()).toString();
        String trimmedExceptionName = exception.substring(exception.lastIndexOf(":") + 1).trim();
        Toast.makeText(context, trimmedExceptionName, Toast.LENGTH_SHORT).show();
        Log.i(Utility.getTAG(), trimmedExceptionName);
    }


    public static String getTAG() {
        return TAG;
    }



}
