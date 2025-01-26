package com.example.trackticum;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.trackticum.activities.ComStudConversation;
import com.example.trackticum.activities.ComViewCoordinators;
import com.example.trackticum.activities.SplashScreen;
import com.example.trackticum.activities.StudAnnouncementList;
import com.example.trackticum.activities.StudLogin;
import com.example.trackticum.activities.StudMainActivity;
import com.example.trackticum.activities.StudShowWeekly;
import com.example.trackticum.activities.StudViewWeekly;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Token: " + token);

        // Send the token to your server if needed
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "Notification";
        String body = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "You have a message";

        // Retrieve the data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data Payload: " + remoteMessage.getData());

            String type = remoteMessage.getData().get("type"); // Get the notification type
            showNotification(title, body, type);
        } else {
            showNotification(title, body, null);
        }
    }

    private void showNotification(String title, String message, String type) {
        String channelId = "default_channel_id";
        String channelName = "Default Channel";

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Intent intent;
        switch (type) {
            case "announcement":
                //for student announcement
                intent = new Intent(this, StudAnnouncementList.class);
                break;
            case "weekly_report":
                //for student weekly report
                intent = new Intent(this, StudViewWeekly.class);
                break;
            case "student_message":
                //If student send a message to company
                intent = new Intent(this, ComStudConversation.class);
                break;
            case "user_message":
                //If Coordinator send a message to company
                intent = new Intent(this, ComViewCoordinators.class);
                break;
            case "student_profile":
                // for student fragment profile
                intent = new Intent(this, StudMainActivity.class);
                intent.putExtra("notification_type", type);
                break;
            default:
                intent = new Intent(this, StudLogin.class);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app icon
                .setContentTitle(title)
                .setContentText(Html.fromHtml(message).toString())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.notify(0, notificationBuilder.build());
    }

    private void sendTokenToServer(String token) {
        // Send the token to your backend server for FCM targeting
        Log.d(TAG, "Sending token to server: " + token);
        // Implement your logic here
    }
}
