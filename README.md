# cordova-plugin-simple-notification
"Do we really need yet another push notification plugin?", you might think. Maybe, maybe not. But here it is. I wrote it, because I needed push notification functionality for a cordova project. Naturally, I tried the best maintained plugin, and it asked me to install "Google Toolbox for Mac" on my development machine. What? For iOS? Soon, I learned you can actually use Google FCM for iOS with this plugin, as well as the native APNS. But, you always have to install this toolbox, and because I don't see any reason in sending my notification to Google, just to let them send it to Apple, and because I try to maximize privacy, I wrote this plugin. Now, enough telled, lets have a look at the plugin!

## WARNING - THIS IS BETA SOFTWARE
As long as my project is not finished, I don't have the plugin in productive use. So for now, look at this as beta software.

## Features
- Supports iOS and Android
- Uses native APNS on iOS, FCM on Android
- Provides the basic push features to keep things simple (KISS)

## Requirements
- iOS: Works with iOS 10 or newer (tested on 10.3.3 and 12.1.4 and newer), uses Swift 4
- Android: Should work with at least 4.4 (tested on 9)

## Installation:
```shell
cordova plugin add https://github.com/TheJKM/cordova-plugin-simple-notification
```
Note: this plugin will not be available via npm as long as it has beta state

## Usage
The plugin creates a global `simplenotification` object, where all functions are located.

### Notification payload structure
This plugin is designed to work with a predefined notification structure. It awaits some predefined values:
- title: The title shown in the notification
- content / body: The longer text shown in the notificaiton

Note: not all values are required - you can provide only custom data, only title and content, and of course both. If you provide neither title nor content no notification will be shown. On the other side, without custom data the message event (see below) won't be called in background.

#### iOS

```json
{
    "alert": {
        "title": "Your impressive title",
        "body": "Your impressive content",
        "badge": 2,
        "sound": "default"
    },
    "data": {
        "anything": "you want"
    }
}
```

#### Android

```json
{
    "data": {
        "title": "Your impressive title",
        "content": "Your impressive content",
        "channel": "importantMessages",
        "anything": "you want",
    }
}
```

Note: You also need to specify the devices you want the message to receive, but as that's independent from the plugin, we won't cover it here.

##### Why don't we use the `notification` property on Android?

If we provide title and content via the notification property, the app won't receive those while in background. So, we are unable to deliver a heads up notification on Android 5 and newer. As a solution, we provide title and content via the data property, and the app will fire a notification depending on the importance value while in background.

## Receiving messages

As long as the app is in foreground, every notification will trigger the message event (see below), and it will deliver the whole notification. This is how APNS and FCM works and nothing the plugin can change. So, it's up to you to display the notification or do anything else.

While in background, if you provide a title and / or a body/content, the plugin will display a notification (completely handled by the system on iOS, handled by the plugin on android). If (and only if) you provide custom data, the message event will be called even in background mode. The system gives you a limited amount of time to do some tasks, after this time it will stop your process. This is nothing the plugin can modify, so please look up in the system documentation if you want to learn more.

## Pluign methods

### Register message event
You should provide a function which is called when the plugin receives a notification while the app is in foreground, or when the plugin recevies a notification with custom data even while the app is in background.
```js
simplenotification.on("message", successCallback, errorCallback);
```
`successCallback` will be called when a notification is received. It provides one parameter containing the notification content as a json string.

`errorCallback` will be called when a notificaiton is received, but the plugin was unable to process it.

### Request permissions to send push notifications (iOS only)
```js
simplenotification.requestPermission(options, successCallback, errorCallback);
```
`options`: An array of strings with the permissions you want to request. Valid values are `alert` for displaying text notifications, `badge` for displaying badge numbers, and `sound` for playing sounds.

`successCallback` will be called after the user made a decision. Returns true if the user granted permissions.

`errorCallback` will be called and provide a notice if the options array is empty, or if anything fails on the native side.

If you call this on Android, you will get a notice via the error callback.

### Check permissions (iOS only)
```js
simplenotification.checkPermission(successCallback, errorCallback);
```

`successCallback` will be called after the user made a decision. It returns an object with:

```json
{
    "permissions": true,
    "alert": true,
    "badge": true,
    "sound": true,
    "lockscreen": true,
    "notificationcenter": true
}
```

These parameters say:
- permissions: Boolean, if notifications in general are allowed
- alert: Boolean, if the user allowed showing alerts
- badge: Boolean, if the user allowed showing a badge number
- sound: Boolean, if the user allowed sound
- lockscreen: Boolean, if the user allowed showing notifications on lockscreen
- notificationcenter: Boolean, if the user allowed showing notifications in notification center

`errorCallback` will be called if anything fails on the native side.

If you call this on Android, you will get a notice via the error callback.

### Register for push notifications
```js
simplenotification.push.register(successCallback, errorCallback);
```

`successCallback` will be called as soon as the system retrieved a token for the device, whith this token as single parameter.

`errorCallback` will be called if anything fails on the native side.

### Receive push token
You don't have to store the token, the plugin does it for you. After calling `register`, you can simply get the token with 

```js
simplenotification.push.getToken();
```

for the rest of the session. The function directly returns the token.

### Set badge number (currently iOS only)
```js
simplenotification.badge.set(newNumber, successCallback, errorCallback);
```

`newNumber` Integer. The new number for your app's icon.

`successCallback` will be called as soon as the new badge number is set.

`errorCallback` will be called if anything fails on the native side.

If you call this on Android, you will get a notice via the error callback.

### Get actual badge number (currently iOS only)
```js
simplenotification.badge.get(successCallback, errorCallback);
```

`successCallback` will return the actual badge number as first parameter.

`errorCallback` will be called if anything fails on the native side.

If you call this on Android, you will get a notice via the error callback.

### Clear notification center
```js
simplenotification.clearNotificationCenter(successCallback, errorCallback);
```

Deletes all notifications currently shown in notification center.

`successCallback` will be called as soon as all notifications are removed.

`errorCallback` will be called if anything fails on the native side.

### Create channel (Android only, has no effect on Android 7 and earlier)
```js
simplenotification.channels.create(id, name, description, importance, successCallback, errorCallback);
```

`id` String. An internal id for the new channel. This can be anything and will not be shown to the user.

`name` String. A human readable name for the new channel. Use a significant name, this is what the user will see in the system settings.

`description` String. A human readable description for the new channel. Describe what notifications will be delivered through this channel. This description will be shown in the system settings.

`importance` Integer. Provide a level of importance to determine how the notifications of this channel will be shown. Note that the user can change this in the system settings.

`successCallback` will be called as soon as the new channel was created.

`errorCallback` will be called if anything fails on the native side.

Possible importance values:
- 0 translates to IMPORTANCE_MIN
- 1 translates to IMPORTANCE_LOW
- 3 translates to IMPORTANCE_HIGH
- anything else translates to IMPORTANCE_DEFAULT

If you call this on iOS, you will get a notice via the error callback.

### Delete channel (Android only, has no effect on Android 7 and earlier)
```js
simplenotification.channels.delete(id, successCallback, errorCallback);
```

`id` String. The internal id of the channel, specified at creating it.

`successCallback` will be called as soon as the channel was removed.

`errorCallback` will be called if anything fails on the native side.

If you call this on iOS, you will get a notice via the error callback.

### Detect if the app is in background state
```js
simplenotification.isBackground();
```

Returns a boolean indicating if the app is currently running in background (true) or foreground (false) state. The plugin needs this internally, so I thought, why should I keep it private.

## License
Copyright 2019 Johannes Kreutz.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
