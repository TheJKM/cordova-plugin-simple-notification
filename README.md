# cordova-plugin-simple-notification
"Do we really need yet another push notification plugin?", you might think. Maybe, maybe not. But here it is. I wrote it, because I needed push notification functionality for a cordova project. Naturally, I tried the best maintained plugin, and it asked me to install "Google Toolbox for Mac" on my development machine. What? For iOS? Soon, I learned you can actually use Google FCM for iOS with this plugin, as well as the native APNS. But, you always have to install this toolbox, and because I don't see any reason in sending my notification to Google, just to let them send it to Apple, and because I try to maximize privacy, I wrote this plugin. Now, enough telled, lets have a look at the plugin!

## WARNING - THIS IS BETA SOFTWARE
As long as my project is not finished, I don't have the plugin in productive use. So for now, look at this as beta software.

## Features
- Supports iOS and Android
- Uses native APNS on iOS, FCM on Android
- Provides the basic push features to keep things simple (KISS)

## Requirements
- iOS: Works with iOS 10 or newer (tested on 12.1.4 and newer), uses Swift 4
- Android: Should work with at least 4.4 (tested on 9)

## Installation:
```shell
cordova plugin add https://github.com/TheJKM/cordova-plugin-simple-notification
```
Note: this plugin will not be available via npm as long as it has beta state

## Usage
The plugin creates a global `simplenotification` object, where all functions are located.

### Notification payload structure
This plugin is designed to work with a predefined notification structure.

#### iOS


#### Android


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

`successCallback` will be called after the user made a decision. 

`errorCallback` will be called and provide a notice if the options array is empty, or if anything fails on the native side.

If you call this on Android, you will get a notice via the error callback.

### Check permissions (iOS only)
```js
simplenotification.checkPermission(successCallback, errorCallback);
```

`successCallback` will be called after the user made a decision. It returns an object with:



`errorCallback` will be called if anything fails on the native side.

If you call this on Android, you will get a notice via the error callback.

### Register for push notifications


### Receive push token


### Set badge number (currently iOS only)


### Get actual badge number (currently iOS only)


### Clear notification center


### Create channel (Android only, has no effect on Android 7 and earlier)


### Delete channel (Android only, has no effect on Android 7 and earlier)


### Detect if the app is in background state


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
