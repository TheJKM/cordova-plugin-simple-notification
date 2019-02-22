/*
 * SimpleNotification - cordova-plugin-simple-notification
 * Native interface for iOS (APNS)
 *
 * Copyright 2019 Johannes Kreutz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Foundation
import UserNotifications

@objc(CDVSimpleNotification) // Make cordova see the class from objective c
class CDVSimpleNotification: CDVPlugin {
    // MARK: Permissions
    // Ask user for permission
    @objc func requestPermission(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: ({
            var authorizationOptions: UNAuthorizationOptions = []
            for argument in command.arguments {
                switch argument {
                case "alert" as String:
                    authorizationOptions.insert(.alert)
                case "badge" as String:
                    authorizationOptions.insert(.badge)
                case "sound" as String:
                    authorizationOptions.insert(.sound)
                default:
                    NSLog("SimpleNotificationPlugin: Ignoring unsupported permission '\(argument)'");
                }
            }
            UNUserNotificationCenter.current().requestAuthorization(options: authorizationOptions) {
                granted, error in
                guard granted else {
                    self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: false), callbackId: command.callbackId)
                    return
                }
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: true), callbackId: command.callbackId)
            }
        }))
    }
    // Check user decision and settings
    @objc func getNotificationSettings(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: ({
            UNUserNotificationCenter.current().getNotificationSettings {
                settings in
                var notificationOptions: Dictionary<String, Bool> = [:]
                notificationOptions["permissions"] = settings.authorizationStatus == .authorized
                notificationOptions["alert"] = settings.alertSetting == .enabled
                notificationOptions["badge"] = settings.badgeSetting == .enabled
                notificationOptions["sound"] = settings.soundSetting == .enabled
                notificationOptions["lockscreen"] = settings.lockScreenSetting == .enabled
                notificationOptions["notificationcenter"] = settings.notificationCenterSetting == .enabled
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: notificationOptions), callbackId: command.callbackId)
            }
        }))
    }
    // MARK: General
    // Set badge number
    @objc func setBadgeNumber(_ command: CDVInvokedUrlCommand) {
        UIApplication.shared.applicationIconBadgeNumber = command.argument(at: 0) as! Int
        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: UIApplication.shared.applicationIconBadgeNumber), callbackId: command.callbackId)
    }
    // Get actual badge number
    @objc func getBadgeNumber(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus_OK, messageAs: UIApplication.shared.applicationIconBadgeNumber), callbackId: command.callbackId)
    }
    // Clear notification center
    @objc func clearNotificationCenter(_ command: CDVInvokedUrlCommand) {
        UNUserNotificationCenter.current().removeAllDeliveredNotifications()
    }
    // MARK: Push notifications
    // Register for push notifications
    @objc func registerForPushNotifications(_ command: CDVInvokedUrlCommand) {
        UIApplication.shared.registerForRemoteNotifications()
    }
    // Push registration finished
    func registrationSucceeded(token: String) {
        self.commandDelegate.evalJs("window.simplenotification.nativeAPI.pushRegistration.success(\"" + token + "\")")
    }
    // Push registraion failed
    func registrationFailed(error: Error) {
        self.commandDelegate.evalJs("window.simplenotification.nativeAPI.pushRegistration.error(\"" + error.localizedDescription + "\")")
    }
    // Got remote message
    func gotMessage(payload: [String: AnyObject]) {
        // Create unified return structure
        let notificationData: NSMutableDictionary = [:]
        if let aps = payload["aps"] as? NSDictionary {
            if let alert = aps["alert"] as? NSDictionary {
                if let title = alert["title"] as? String {
                    notificationData["title"] = title
                }
                if let content = alert["body"] as? String {
                    notificationData["content"] = content
                }
            }
            if let badge = aps["badge"] as? Int {
                notificationData["badge"] = badge
            }
        }
        if let data = payload["data"] as? NSDictionary {
            notificationData["data"] = data
        }
        self.commandDelegate.evalJs("window.simplenotification.nativeAPI.onMessage('" + dictionaryToJson(input: notificationData) + "')")
    }
    // Convert NSDictionary to JSON string
    private func dictionaryToJson(input: NSDictionary) -> String {
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: input)
            return String(data: jsonData, encoding: String.Encoding.utf8)!
        } catch {
            print("SimpleNotification Plugin:" + error.localizedDescription)
            return "{\"error\":\"JSON creation failed\"}"
        }
    }
}
