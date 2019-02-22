/*
 * SimpleNotification - cordova-plugin-simple-notification
 * AppDelegate extension - adaption for native callbacks
 * © 2019 by Johannes Kreutz
 */

// COMMUNICATION WITH NATIVE API
extension AppDelegate {
    // Helper: get cordova plugin instance
    private func getCDVPlugin() -> CDVSimpleNotification {
        return self.viewController.getCommandInstance("CDVSimpleNotification") as! CDVSimpleNotification
    }
    // Native callback: token received
    override open func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let tokenParts = deviceToken.map { data in String(format: "%02.2hhx", data) }
        let token = tokenParts.joined()
        getCDVPlugin().registrationSucceeded(token: token)
    }
    // Native callback: token error
    override open func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        getCDVPlugin().registrationFailed(error: error)
    }
    // Got remote notification event
    override open func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        guard let payload = userInfo as? [String: AnyObject] else {
            completionHandler(.failed)
            return
        }
        if (application.applicationState == UIApplication.State.background) {
            let aps = payload["aps"] as? [String: AnyObject]
            if (aps?["content-available"] as? NSNumber == 1) {
                getCDVPlugin().gotMessage(payload: payload)
            }
        } else {
            getCDVPlugin().gotMessage(payload: payload)
        }
        completionHandler(.newData)
    }
    // Fetch rare case when a notification arrives as the user closes the app
    override open func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        super.application(application, didFinishLaunchingWithOptions: launchOptions)
        let remotePayload = launchOptions?[.remoteNotification] as? [String: Any]
        if (remotePayload != nil) {
            NSLog("Launched from push notification.");
            let payload = remotePayload! as [String: AnyObject]
            getCDVPlugin().gotMessage(payload: payload)
        }
        return true
    }
}
