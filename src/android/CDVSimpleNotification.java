/*
 * SimpleNotification - cordova-plugin-simple-notification
 * Native interface for Android (FCM)
 * © 2019 by Johannes Kreutz
 */
package com.jkmsoftware.simplenotification;

import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.iid.FirebaseInstanceId;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class CDVSimpleNotification extends CordovaPlugin {
    // EXECUTE JAVASCRIPT WITHOUT CALL

    /**
     * Execute the given string as javascript on the cordova webview
     * @param strJS The javascript string
     */
    private void executeJavascript(final String strJS) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + strJS);
            }
        });
    }

    // TOKEN MANAGEMENT
    private String token = "";

    /**
     * Store the given token to re-use it in the actual session
     * @param token The token
     */
    public void storeToken(String token) {
        this.token = token;
    }

    // STATIC METHODS FOR EXTERNAL SERVICE
    private static CDVSimpleNotification instance;

    /**
     * Returns the stored instance of the plugin
     * @return An instance of the plugin
     */
    public static CDVSimpleNotification getInstance() {
        return instance;
    }

    // INITIALIZE THE PLUGIN

    /**
     * Plugin initializer. Stores an instance as static so external services can access some methods
     * @param cordova The cordova object
     * @param webView The cordova webview object
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        instance = this;
    }

    // HELPER FUNCTIONS

    /**
     * Translates an internal priority to the values of NotificationManager
     * @param commonPrio Internal proirity value
     * @return NotificationManager priority
     */
    private int translatePriotiry(int commonPrio) {
        switch (commonPrio) {
            case 0:
                return NotificationManager.IMPORTANCE_MIN;
            case 1:
                return NotificationManager.IMPORTANCE_LOW;
            case 3:
                return NotificationManager.IMPORTANCE_HIGH;
            default:
                return NotificationManager.IMPORTANCE_DEFAULT;
        }
    }


    // APP STATE
    private static boolean appIsInBackgroundState = false;

    // MAIN LOGIC
    private static String backgroundChannelId = "default";
    private static int backgroundPriority = NotificationCompat.PRIORITY_DEFAULT;
    private static boolean backgroundAutoCancel = false;

    /**
     * Main entrypoint, passess calls from javascript to the right logic
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True if the identifier was found, else false
     * @throws JSONException Any errors in json parsing
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("clearNotificationCenter")) {
            this.clearNotificationCenter(callbackContext);
            return true;
        } else if (action.equals("registerForPushNotifications")) {
            this.registerForPushNotifications(callbackContext);
            return true;
        } else if (action.equals("createNotificationChannel")) {
            this.createNotificationChannel(args.getString(0), args.getString(1), args.getString(2), args.getInt(3), callbackContext);
            return true;
        } else if (action.equals("deleteNotificationChannel")) {
            this.deleteNotificationChannel(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("init")) {
            return true;
        } else if (action.equals("setBackgroundChannelId")) {
            backgroundChannelId = args.getString(0);
            return true;
        } else if (action.equals("changedToBackgroundState")) {
            appIsInBackgroundState = true;
            return true;
        } else if (action.equals("changedToForegroundState")) {
            appIsInBackgroundState = false;
            return true;
        }
        return false;
    }

    // GENERAL

    /**
     * Removes all notifications from the notification center
     * @param callbackContext The callback context used when calling back into JavaScript.
     */
    private void clearNotificationCenter(CallbackContext callbackContext) {
        NotificationManager notificationManager = (NotificationManager) cordova.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        callbackContext.success("Cleared notification center");
    }

    /**
     * Create a notificaiton channel for api 26 and newer
     * @param id Channel ID
     * @param name Channel name
     * @param description Channel description
     * @param imp Channel importance, plugin internal value
     * @param callbackContext The callback context used when calling back into JavaScript.
     */
    private void createNotificationChannel(String id, String name, String description, int imp, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(id, name, translatePriotiry(imp));
                    channel.setDescription(description);
                    NotificationManager notificationManager = (NotificationManager) cordova.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.createNotificationChannel(channel);
                    callbackContext.success("Channel created");
                } else {
                    callbackContext.success("Notice: Unsupported on this version of Android (SDK " + Build.VERSION.SDK_INT + ").");
                }
            }
        });
    }

    /**
     * Removes a notificaiton channel for api 26 and newer
     * @param id Channel ID
     * @param callbackContext The callback context used when calling back into JavaScript.
     */
    private void deleteNotificationChannel(String id, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager notificationManager = (NotificationManager) cordova.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.deleteNotificationChannel(id);
                    callbackContext.success("Channel removed");
                } else {
                    callbackContext.success("Notice: Unsupported on this version of Android (SDK " + Build.VERSION.SDK_INT + ").");
                }
            }
        });
    }

    /**
     * Register for fcm receive and return the fcm device token
     * @param callbackContext The callback context used when calling back into JavaScript.
     */
    private void registerForPushNotifications(CallbackContext callbackContext) {
        String token;
        // Use stored token if available, else call firebase to get the token
        if (this.token.length() > 0) {
            token = this.token;
        } else {
            token = FirebaseInstanceId.getInstance().getToken();
        }
        callbackContext.success(token);
    }

    // CALLBACKS

    /**
     * Gets called when a notification is received, unify structure and forward to javascript
     * @param remoteMessage The remote content object from fcm
     */
    static void onNotification(RemoteMessage remoteMessage) {
        // Store data in a map
        Map<String, String> data = remoteMessage.getData();
        // Create json string from notification payload - unify structure
        Map<String, Object> messageObject = new HashMap<String, Object>();
        if (data.containsKey("title")) {
            messageObject.put("title", data.get("title"));
            data.remove("title");
        }
        if (data.containsKey("content")) {
            messageObject.put("content", data.get("content"));
            data.remove("content");
        }
        if (data.containsKey("channel")) {
            messageObject.put("channel", data.get("channel"));
            data.remove("channel");
        }
        messageObject.put("data", data);
        if (appIsInBackgroundState) {
            getInstance().cordova.getActivity().runOnUiThread((new Runnable() {
                public void run() {
                    String channelId = (messageObject.containsKey("channel")) ? (String)messageObject.get("channel") : "default";
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getInstance().cordova.getContext(), backgroundChannelId)
                            .setSmallIcon(getInstance().cordova.getContext().getApplicationInfo().icon)
                            .setPriority(backgroundPriority)
                            .setAutoCancel(backgroundAutoCancel);
                    if (messageObject.containsKey("title")) {
                        notificationBuilder.setContentTitle((String)messageObject.get("title"));
                    }
                    if (messageObject.containsKey("content")) {
                        notificationBuilder.setContentText((String)messageObject.get("content"));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationBuilder.setChannelId(channelId);
                    }
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getInstance().cordova.getActivity());
                    notificationManager.notify(0, notificationBuilder.build());
                }
            }));
        }
        if (!appIsInBackgroundState || data.size() > 0) {
            // Call javascript api with payload as json
            JSONObject notificationJson = new JSONObject(messageObject);
            getInstance().executeJavascript("simplenotification.nativeAPI.onMessage('" + notificationJson.toString() + "')");
        }
    }

    /**
     * Gets called when a new token is initially created, store it for later use in the session
     * @param token FCM token
     */
    static void onToken(String token) {
        // Store token
        getInstance().storeToken(token);
    }
}
