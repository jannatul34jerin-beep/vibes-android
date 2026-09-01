package com.vibes.social;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/** Receives high-priority FCM data messages for Vibes calls. */
public class VibesFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "vibes_calls";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        getSharedPreferences("vibes_native", MODE_PRIVATE)
                .edit().putString("fcm_token", token).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String, String> data = message.getData();
        String type = value(data, "type", "");
        if (!"incoming_call".equals(type) && !"incoming_group_call".equals(type)) return;

        String caller = value(data, "caller_name", "Vibes call");
        String callType = value(data, "call_type", "audio");
        String callId = value(data, "call_id", "");
        String roomId = value(data, "room_id", "");
        showCallNotification(caller, callType, callId, roomId, type);
    }

    private String value(Map<String, String> data, String key, String fallback) {
        String v = data.get(key);
        return v == null || v.isEmpty() ? fallback : v;
    }

    private void showCallNotification(String caller, String callType, String callId, String roomId, String pushType) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Incoming calls", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Incoming Vibes audio and video calls");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            nm.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("vibes_push_type", pushType);
        intent.putExtra("vibes_call_id", callId);
        intent.putExtra("vibes_room_id", roomId);
        intent.putExtra("vibes_call_type", callType);
        intent.putExtra("vibes_caller_name", caller);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent contentIntent = PendingIntent.getActivity(this, 7002, intent, flags);

        Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.sym_call_incoming)
         .setContentTitle(caller)
         .setContentText("video".equals(callType) ? "Incoming video call" : "Incoming audio call")
         .setCategory(Notification.CATEGORY_CALL)
         .setPriority(Notification.PRIORITY_MAX)
         .setVisibility(Notification.VISIBILITY_PUBLIC)
         .setAutoCancel(true)
         .setContentIntent(contentIntent);
        nm.notify(7001, b.build());
    }
}
