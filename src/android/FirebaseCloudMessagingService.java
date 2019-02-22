/*
 * SimpleNotification - cordova-plugin-simple-notification
 * Native interface for Android (FCM) - Message listener
 * © 2019 by Johannes Kreutz
 */

package com.jkmsoftware.simplenotification;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseCloudMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        CDVSimpleNotification.onNotification(remoteMessage);
    }
}
