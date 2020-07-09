package com.animesh.notfallapp.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.animesh.notfallapp.R;
import com.animesh.notfallapp.activities.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class NotificationService extends Service {

    private final String channelId = "default_channel_id";
    private final String channelDescription = "Default Channel";
    final int NOTIFY_ID = 0;
    private NotificationCompat.Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference();

        DatabaseReference userRef = userDatabase.child("users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationManager notifManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notifManager != null) {

                    NotificationChannel notificationChannel = notifManager.getNotificationChannel(channelId);

                    if (notificationChannel == null) {
                        notificationChannel = new NotificationChannel(channelId, "title", importance);
                        notificationChannel.enableVibration(true);
                        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                        notifManager.createNotificationChannel(notificationChannel);

                    }

                    builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                    builder.setContentTitle("title")                            // required
                            .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                            .setContentText(getApplicationContext().getString(R.string.app_name)) // required
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setTicker("title")
                            .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                }

                Notification notification = builder.build();

                if (notifManager != null) {
                    notifManager.notify(NOTIFY_ID, notification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
