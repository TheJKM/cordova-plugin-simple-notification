/*
 * SimpleNotification - cordova-plugin-simple-notification
 * Cordova JS interface
 * © 2019 by Johannes Kreutz
 */

// Include cordova dependencies
var exec = require('cordova/exec');
// Globally store plugin name
var CDV_PLUGIN_NAME = 'CDVSimpleNotification';

// Public object
var simplenotification = {
  on: function(event, successCallback, errorCallback) {
    switch(event) {
      case 'message':
        simplenotification_callbacks.message.success = successCallback;
        simplenotification_callbacks.message.error = errorCallback;
        break;
    }
  },
  requestPermission: function(options, successCallback, errorCallback) {
    if (device.platform == "iOS") {
      if (Array.isArray(options) && options.length > 0) {
        simplenotification_callbacks.permissions.success = successCallback;
        exec(simplenotification_private.gotPermissionResult, errorCallback, CDV_PLUGIN_NAME, 'requestPermission', options);
      } else {
        errorCallback("SimpleNotification Plugin Error: Options is not an array or empty.");
      }
    } else {
      errorCallback("This feature is not supported on your platform.");
    }
  },
  checkPermission: function(successCallback, errorCallback) {
    if (device.platform == "iOS") {
      exec(successCallback, errorCallback, CDV_PLUGIN_NAME, 'getNotificationSettings', []);
    } else {
      errorCallback("This feature is not supported on your platform.");
    }
  },
  push: {
    register: function(successCallback, errorCallback) {
      simplenotification_callbacks.pushRegistration.success = successCallback;
      simplenotification_callbacks.pushRegistration.error = errorCallback;
      exec(simplenotification.nativeAPI.pushRegistration.success, simplenotification.nativeAPI.pushRegistration.error, CDV_PLUGIN_NAME, 'registerForPushNotifications', []);
    },
    getToken: function() {
      if (simplenotification_private.pushToken === "") {
        return "SimpleNotification Plugin Error: no push token stored, please call push.register!";
      } else {
        return simplenotification_private.pushToken;
      }
    }
  },
  badge: {
    set: function(newNumber, successCallback, errorCallback) {
      if (device.platform == "iOS") {
        exec(successCallback, errorCallback, CDV_PLUGIN_NAME, 'setBadgeNumber', [newNumber]);
      } else {
        errorCallback("This feature is not supported on your platform.");
      }
    },
    get: function(successCallback, errorCallback) {
      if (device.platform == "iOS") {
        exec(successCallback, errorCallback, CDV_PLUGIN_NAME, 'getBadgeNumber', []);
      } else {
        errorCallback("This feature is not supported on your platform.");
      }
    }
  },
  clearNotificationCenter: function(successCallback, errorCallback) {
    exec(successCallback, errorCallback, CDV_PLUGIN_NAME, 'clearNotificationCenter', [])
  },
  channels: {
    create: function(id, name, description, importance, successCallback, errorCallback) {
      if (device.platform == "Android") {
        exec(successCallback, errorCallback, CDV_PLUGIN_NAME, 'createNotificationChannel', [id, name, description, importance]);
      } else {
        errorCallback("This feature is not supported on your platform.");
      }
    },
    delete: function(id, successCallback, errorCallback) {
      if (device.platform == "Android") {
        exec(successCallback, errorCallback, CDV_PLUGIN_NAME, 'deleteNotificationChannel', [id]);
      } else {
        errorCallback("This feature is not supported on your platform.");
      }
    }
  },
  isBackground: function() {
    return simplenotification_private.isBackground;
  },
  nativeAPI: {
    pushRegistration: {
      success: function(token) {
        simplenotification_private.pushToken = token;
        var pushProvider = "undefined";
        if (device.platform == "iOS") {
          pushProvider = "apns";
        } else if (device.platform == "Android") {
          pushProvider = "fcm";
        }
        let returnObject = {
          platform: pushProvider,
          token: token,
        }
        if (simplenotification_callbacks.pushRegistration.success != null) {
          simplenotification_callbacks.pushRegistration.success(returnObject);
        }
      },
      error: function(error) {
        console.log("SimpleNotification Plugin Error: Something unexpected happened. Native says: " + error);
        simplenotification_callbacks.pushRegistration.error("SimpleNotification Plugin Error: Something unexpected happened. Native says: " + error);
      },
    },
    onMessage: function(messageObject) {
      simplenotification_callbacks.message.success(messageObject);
    }
  }
}

// Callbacks
var simplenotification_callbacks = {
  message: {
    success: null,
    error: null,
  },
  pushRegistration: {
    success: null,
    error: null,
  },
  permissions: {
    success: null,
  }
}

// Private elements
var simplenotification_private = {
  // Permissions
  hasPermission: false,
  gotPermissionResult: function(notificationOptions) {
    this.hasPermission = notificationOptions.permissions;
    simplenotification_callbacks.permissions.success(notificationOptions);
  },
  // Push
  pushToken: "",
  init: function() {
    exec(null, null, CDV_PLUGIN_NAME, 'initialize', []);
  },
  // App state
  isBackground: false
}

document.addEventListener('deviceready', function() {
  if (device.platform == 'Android') {
    simplenotification_private.init();
  }
}, false);
document.addEventListener('pause', function() {
  if (device.platform == 'Android') {
    exec(null, null, CDV_PLUGIN_NAME, 'changedToBackgroundState', []);
  }
  simplenotification_private.isBackground = true;
}, false);
document.addEventListener('resume', function() {
  if (device.platform == 'Android') {
    exec(null, null, CDV_PLUGIN_NAME, 'changedToForegroundState', []);
  }
  simplenotification_private.isBackground = false;
}, false);

// Make public parts available
module.exports = simplenotification;
