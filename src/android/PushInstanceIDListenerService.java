/*
 * SimpleNotification - cordova-plugin-simple-notification
 * Native interface for Android (FCM) - Token listener
 * © 2019 by Johannes Kreutz
 */

package com.jkmsoftware.simplenotification;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class PushInstanceIDListenerService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        CDVSimpleNotification.onToken(refreshedToken);
    }
}
