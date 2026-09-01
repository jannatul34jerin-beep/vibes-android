package com.vibes.social;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.PermissionRequest;
import android.Manifest;
import android.content.pm.PackageManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification;
import android.content.Context;
import android.os.Build;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int MEDIA_PERMISSION_REQUEST = 1002;
    private PermissionRequest pendingWebPermission;
    private static final String CALL_CHANNEL_ID = "vibes_calls";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        createCallNotificationChannel();

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.addJavascriptInterface(new NativeBridge(this), "VibesNative");

        webView.setWebViewClient(new WebViewClient());
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1003);
        }
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean cameraOk = android.os.Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                    boolean micOk = android.os.Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                    if (cameraOk && micOk) request.grant(request.getResources());
                    else { pendingWebPermission = request; requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, MEDIA_PERMISSION_REQUEST); }
                });
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> callback,
                                             FileChooserParams fileChooserParams) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                return true;
            }
        });

        if (savedInstanceState == null) {
            webView.loadUrl("file:///android_asset/index.html");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MEDIA_PERMISSION_REQUEST && pendingWebPermission != null) {
            boolean granted = true;
            for (int r : grantResults) if (r != PackageManager.PERMISSION_GRANTED) granted = false;
            if (granted) pendingWebPermission.grant(pendingWebPermission.getResources());
            else pendingWebPermission.deny();
            pendingWebPermission = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                result = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(result);
            filePathCallback = null;
        }
    }

    private void createCallNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CALL_CHANNEL_ID, "Incoming calls", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Incoming audio and video calls on Vibes");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    public static class NativeBridge {
        private final Activity activity;
        NativeBridge(Activity activity) { this.activity = activity; }

        @JavascriptInterface
        public void showIncomingCall(String callerName, String callType, String callId) {
            activity.runOnUiThread(() -> {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("vibes_call_id", callId == null ? "" : callId);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
                PendingIntent contentIntent = PendingIntent.getActivity(activity, 42, intent, flags);
                Notification.Builder b = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? new Notification.Builder(activity, CALL_CHANNEL_ID)
                        : new Notification.Builder(activity);
                b.setSmallIcon(android.R.drawable.sym_call_incoming)
                 .setContentTitle(callerName == null || callerName.isEmpty() ? "Vibes call" : callerName)
                 .setContentText("video".equals(callType) ? "Incoming video call" : "Incoming audio call")
                 .setCategory(Notification.CATEGORY_CALL)
                 .setPriority(Notification.PRIORITY_MAX)
                 .setVisibility(Notification.VISIBILITY_PUBLIC)
                 .setAutoCancel(true)
                 .setContentIntent(contentIntent);
                ((NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE)).notify(7001, b.build());
            });
        }

        @JavascriptInterface
        public void clearIncomingCall() {
            ((NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(7001);
        }

        @JavascriptInterface
        public String getPushToken() {
            return activity.getSharedPreferences("vibes_native", Context.MODE_PRIVATE)
                    .getString("fcm_token", "");
        }

        @JavascriptInterface
        public String getPendingCallJson() {
            Intent i = activity.getIntent();
            if (i == null) return "{}";
            String pushType = safe(i.getStringExtra("vibes_push_type"));
            String callId = safe(i.getStringExtra("vibes_call_id"));
            String roomId = safe(i.getStringExtra("vibes_room_id"));
            String callType = safe(i.getStringExtra("vibes_call_type"));
            String caller = safe(i.getStringExtra("vibes_caller_name"));
            return "{\"type\":\"" + json(pushType) + "\",\"call_id\":\"" + json(callId) + "\",\"room_id\":\"" + json(roomId) + "\",\"call_type\":\"" + json(callType) + "\",\"caller_name\":\"" + json(caller) + "\"}";
        }

        private String safe(String s) { return s == null ? "" : s; }
        private String json(String s) { return safe(s).replace("\\", "\\\\").replace("\"", "\\\""); }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }
}
