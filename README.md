# LinkzlySDK for React Native

![Platform](https://img.shields.io/badge/platforms-iOS%20%7C%20Android-lightgrey?style=flat-square)

LinkzlySDK is a React Native wrapper for deep linking and attribution tracking. Track app installs, opens, and custom events while seamlessly handling Universal Links (iOS) and App Links (Android) for deferred deep linking.

Full documentation: https://docs.linkzly.com/docs/sdk-react-native

Related guides: [Tracking events](https://docs.linkzly.com/docs/sdk-tracking-events), [Tracking purchases](https://docs.linkzly.com/docs/sdk-tracking-purchases), [Integrations setup](https://docs.linkzly.com/docs/sdk-integrations-setup).

## Using an AI coding agent

If you use an AI coding agent (Claude Code, Codex, Cursor, GitHub Copilot, Antigravity), give it the Linkzly skill and ask it to add the SDK to your app: `npx skills add Linkzly/skills`. The skill carries these instructions, cut to your platform, and rules for the SDK behaviours that are easy to get wrong. It asks you for your SDK key and never makes one up. See [Set up with an AI agent](https://docs.linkzly.com/docs/sdk-ai-agents).

## Features

- 🔗 **Universal Links & App Links Support** - Handle deep links automatically
- 📊 **Attribution Tracking** - Track installs, opens, and custom events
- 🎯 **Deferred Deep Linking** - Match users to campaigns after install
- 👤 **User Identification** - Associate events with specific users
- 🤝 **Affiliate Attribution** - Capture and store affiliate click IDs for server-to-server (S2S) conversion tracking
- 🔔 **Push Notifications** - Registration of the Firebase Cloud Messaging (FCM) registration token, plus FCM broadcast topics
- 🎮 **Gaming Intelligence** - Batch event tracking for games with session management
- 💰 **Revenue** - Purchases and refunds with a transaction ID for de-duplication
- 📤 **Event Queue Management** - Offline queuing with manual flush controls
- 🔐 **Privacy-First** - Opt-in/opt-out tracking controls
- 📱 **Advertising Identifiers** - IDFA, GAID, and ATT framework support
- ⚡ **Lightweight** - Zero third-party JavaScript dependencies
- 🔧 **TypeScript** - Full type definitions included

## Requirements

| Component | Value |
|-----------|-------|
| React Native | Tested on React Native 0.82.1. Declared peer range: `react-native >=0.71.2`, `react >=16.13.1` (versions below 0.82.1 are untested) |
| Node.js | Declared by the package: `>= 16.0.0` |
| iOS | Minimum deployment target declared by the SDK: 12.0 |
| Android | Minimum declared by the SDK: API 21 |

## Prerequisites

Before integrating the SDK, set up your app in the Linkzly Console:

1. Go to **Dashboard > Apps** and click "Register App"
2. Enter your iOS **Bundle ID** and/or Android **Package Name**
3. Choose a verification method (Hosted recommended for quick start)
4. Copy your **SDK Key** from the post-creation wizard or from Manage app > Overview > SDK Configuration

Your SDK key starts with `slk_` and uniquely identifies your app.

## Getting Your SDK Key

Your SDK key (`slk_` prefix) authenticates your app with Linkzly's servers. You can find it in:

- **Post-creation wizard**: Displayed prominently right after creating your app
- **Dashboard > Apps > Manage app > Overview > SDK Configuration**: Click the eye icon to reveal, or copy directly

> **Note**: Each app has a unique SDK key. Do not share keys between different apps.

## Installation

### Install from GitHub

**SDK version:** 1.0.17

The package is installed from GitHub. It is not published to the npm registry, so `npm install @linkzly/react-native-sdk` will not find it.

```bash
# npm
npm install github:Linkzly/react-native-linkzly-sdk#1.0.17

# Yarn 1 (classic)
yarn add Linkzly/react-native-linkzly-sdk#1.0.17
```

The package is installed under the name `@linkzly/react-native-sdk`, which is what you import. The tag `1.0.17` is what you pin; the `version` field inside the installed `package.json` reports a different number (`1.4.6` at this tag), which is expected.

### iOS Setup

1. Add LinkzlySDK to your `Podfile` (before `use_native_modules!`):

```ruby
pod 'LinkzlySDK', :git => 'https://github.com/Linkzly/linkzly-ios-sdk.git', :tag => '1.0.7'
```

LinkzlySDK is not on CocoaPods Trunk, so the `:git` and `:tag` form is required; without it `pod install` cannot find the spec.

2. Install CocoaPods dependencies:

```bash
cd ios && pod install && cd ..
```

### Android Setup

The package's own `android/build.gradle` already declares the JitPack repository and depends on the native Android SDK (`com.github.Linkzly:linkzly-android-sdk:1.0.9`), so a default React Native project needs no change to its Gradle files.

If your project sets `RepositoriesMode.FAIL_ON_PROJECT_REPOS` in `android/settings.gradle`, the build stops at the `repositories` block in this package's `android/build.gradle`, and adding JitPack to the settings file does not change that. Set `RepositoriesMode.PREFER_SETTINGS` instead, so that Gradle ignores that block, and list JitPack in the settings file. This was checked in a small Gradle project, not in a full React Native build:

```gradle
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
  repositories {
    google()
    mavenCentral()
    maven { url 'https://jitpack.io' }
  }
}
```

### iOS Deep Link Configuration (Required for Warm Start)

For proper deep link handling on iOS when the app is already running (warm start), you must forward URLs to both React Native's `RCTLinkingManager` and `LinkzlySDK` in your AppDelegate.

> **Note:** This setup was tested on React Native 0.82.1.

#### Step 1: Update Bridging Header

First, add `RCTLinkingManager` to your bridging header so it's accessible from Swift.

**File:** `ios/YourApp-Bridging-Header.h`

```objc
#import <React/RCTLinkingManager.h>
```

> **Tip:** If you don't have a bridging header, create one at `ios/YourApp-Bridging-Header.h` and set it in Xcode:
> Target → Build Settings → "Objective-C Bridging Header" → `YourApp-Bridging-Header.h`

#### Step 2: Add Deep Link Methods to AppDelegate

**File:** `ios/YourApp/AppDelegate.swift`

```swift
import Linkzly

// Add these methods to your AppDelegate class

// MARK: - URL Scheme Handling (yourapp://...)
override func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
    // Forward to React Native's Linking module (required for warm start)
    let handledByRN = RCTLinkingManager.application(app, open: url, options: options)

    // Forward to LinkzlySDK for attribution tracking
    _ = LinkzlySDK.handleUniversalLink(url)

    return handledByRN
}

// MARK: - Universal Links Handling (https://...)
override func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
    // Forward to React Native's Linking module
    let handledByRN = RCTLinkingManager.application(application, continue: userActivity, restorationHandler: restorationHandler)

    // Forward to LinkzlySDK for attribution tracking
    _ = LinkzlySDK.handleUniversalLink(userActivity)

    return handledByRN
}
```

> **Do NOT use `import React_RCTLinkingManager`** – this module name varies between React Native versions and architecture settings. Using the bridging header approach ensures compatibility across all versions.

---

#### Alternative: Objective-C Setup

If your AppDelegate is in Objective-C (`AppDelegate.m` or `AppDelegate.mm`):

```objc
#import <React/RCTLinkingManager.h>
#import <Linkzly/Linkzly-Swift.h>

// URL Scheme handling
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
    // Forward to React Native's Linking module (required for warm start)
    BOOL handledByRN = [RCTLinkingManager application:application openURL:url options:options];

    // Forward to LinkzlySDK for attribution tracking
    [LinkzlySDK handleUniversalLink:url];

    return handledByRN;
}

// Universal Links handling
- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray<id<UIUserActivityRestoring>> *))restorationHandler {
    // Forward to React Native's Linking module
    BOOL handledByRN = [RCTLinkingManager application:application continueUserActivity:userActivity restorationHandler:restorationHandler];

    // Forward to LinkzlySDK for attribution tracking
    [LinkzlySDK handleUniversalLink:userActivity];

    return handledByRN;
}
```

---

> **Important:** Without this setup, deep links will NOT work when the app is in the background (warm start). The `RCTLinkingManager` calls are required for React Native's `Linking.addEventListener('url')` to receive events.

### Android Deep Link Configuration (Required for Warm Start)

For proper deep link handling on Android when the app is already running (warm start), you must override `onNewIntent()` in your MainActivity:

**File:** `android/app/src/main/java/com/yourapp/MainActivity.kt`

```kotlin
import android.content.Intent
import com.linkzly.reactnative.LinkzlyReactNativeModule

class MainActivity : ReactActivity() {
  // ... existing code ...

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    setIntent(intent)
    LinkzlyReactNativeModule.getLatestInstance()?.handleIntent(intent)
  }
}
```

**Why is this needed?**
React Native's `Linking.addEventListener` has a known issue on Android where it doesn't reliably fire when the app receives a deep link while already running. This override ensures deep links are processed directly through the LinkzlySDK native module.

**After adding this:**
- You can remove any `Platform.OS === 'android'` workarounds using React Native's Linking API
- Deep links will work reliably on both cold and warm starts
- The SDK handles everything automatically

## Quick Start

```typescript
import React, { useEffect } from 'react';
import LinkzlySDK, { Environment } from '@linkzly/react-native-sdk';

function App() {
  useEffect(() => {
    // Configure SDK
    LinkzlySDK.configure('YOUR_SDK_KEY', Environment.PRODUCTION);

    // Listen for deep links
    const unsubscribe = LinkzlySDK.addDeepLinkListener((data) => {
      console.log('Deep link received:', data);
      // Navigate based on data.path and data.parameters
    });

    return () => unsubscribe();
  }, []);

  return <YourApp />;
}
```

With the default `autoHandleDeepLinks: true`, `configure` reads the launch URL (`Linking.getInitialURL()`) and listens for `Linking` `url` events, so both cold starts and warm starts reach your listener.

`configure` resolves even when the native SDK refused the key. A key that is not `slk_` followed by at least 40 hex characters (a placeholder, for example) leaves the SDK unconfigured for the life of the app process, and later `configure` calls are ignored. After you put the real key in, stop the app and start it again; a JavaScript reload is not enough.

### Cold Start Handling (App Launched from Link)

A listener added after a deep link was delivered is called straight away with the last deep link data, so subscribing before `configure` is enough. You only need to read `Linking.getInitialURL()` yourself when you turn automatic handling off (`autoHandleDeepLinks: false`) and pass the URL to `handleUniversalLink`:

```typescript
import { useEffect } from 'react';
import { Linking } from 'react-native';
import LinkzlySDK, { Environment } from '@linkzly/react-native-sdk';

export function useManualDeepLinks(): void {
  useEffect(() => {
    const unsubscribe = LinkzlySDK.addDeepLinkListener((data) => {
      console.log('Deep link received:', data);
    });

    LinkzlySDK.configure('YOUR_SDK_KEY', Environment.PRODUCTION, {
      autoHandleDeepLinks: false,
    }).then(async () => {
      const url = await Linking.getInitialURL();
      if (url) {
        await LinkzlySDK.handleUniversalLink(url);
      }
    });

    return () => unsubscribe();
  }, []);
}
```

> **Important:** `Linking.getInitialURL()` only works on cold starts. For warm starts (app in background), the SDK's `addDeepLinkListener` receives the link.

## Deep Link Handling

```typescript
const unsubscribe = LinkzlySDK.addDeepLinkListener((data) => {
  console.log('Path:', data.path);           // e.g., "/product"
  console.log('Parameters:', data.parameters); // e.g., { id: "123" }
  console.log('Smart Link ID:', data.smartLinkId);
  console.log('Click ID:', data.clickId);
});
```

### Listener Cleanup

`addDeepLinkListener` and `addUniversalLinkListener` return an unsubscribe function. Call it in your cleanup, or call `LinkzlySDK.removeAllListeners()` to drop every listener.

### Manual Deep Link Handling

`configure` options: `autoHandleDeepLinks` (default `true`) and `autoTrackAppOpens` (default `true`; `configure` tracks an app open in the background).

```typescript
LinkzlySDK.configure('YOUR_SDK_KEY', Environment.PRODUCTION, {
  autoHandleDeepLinks: false
});

// Then manually handle URLs
await LinkzlySDK.handleUniversalLink(url);
```

## Event Tracking

The React Native SDK bridges to the native iOS/Android implementations to automatically track lifecycle events and provides methods for manual event tracking.

### 1. Lifecycle Events (Automatic)

The native SDK automatically tracks application lifecycle events:
*   **Install**: Tracked on the first launch.
*   **Open**: Tracked when the app comes to the foreground after at least 30 seconds away, and once by `configure` unless you pass `autoTrackAppOpens: false`.

Sessions are automatic: the session ends when the app goes to the background. You do not start or end app sessions yourself (the gaming session methods are `startGamingSession` and `endGamingSession`).

```typescript
import LinkzlySDK from '@linkzly/react-native-sdk';

// The SDK sends the install itself on the first launch of a new install, so calling
// trackInstall on a new install sends a second install event. On iOS, addDeepLinkListener
// receives the deferred DeepLinkData from the automatic install. On Android the listener
// is fed only from handled links, so trackInstall is the only way to get it there; call
// it once, behind your own first-launch flag (the flag stops a third event, not the second).
await LinkzlySDK.trackInstall();

// The SDK already sends opens itself (when the app returns to the foreground after more than
// 30 seconds, and from configure unless autoTrackAppOpens is false). Each explicit trackOpen
// call sends one more open event: call it only when you need the DeepLinkData it returns.
await LinkzlySDK.trackOpen();
```

### 2. Commerce Events

Track revenue and in-app purchases. `revenue`, `currency` and `transactionId` are required; `transactionId` is the de-duplication key (a repeat of the same id is discarded). A refund reuses the original `transactionId`. Purchases are recorded only when Revenue & LTV is enabled for your organisation. See [Tracking purchases](https://docs.linkzly.com/docs/sdk-tracking-purchases).

```typescript
import LinkzlySDK from '@linkzly/react-native-sdk';

// Track a purchase
await LinkzlySDK.trackPurchase({
  revenue: 9.99,
  currency: 'USD',
  transactionId: 'order-12345',
  productId: 'premium_monthly',
  revenueType: 'subscription', // 'iap' | 'subscription' | 'other'
});

// Track a refund of that purchase
await LinkzlySDK.trackRefund({
  revenue: 9.99,
  currency: 'USD',
  transactionId: 'order-12345',
});
```

### 3. Custom Events

Track the user actions that matter in your app.

**Register the name first.** A custom event is kept only if its name is registered for the app in the console. An unregistered name is dropped, and on the default batched path the call and the flush still report success. See [Register each name in the console](https://docs.linkzly.com/docs/sdk-tracking-events#register-each-name-in-the-console).

```typescript
import LinkzlySDK from '@linkzly/react-native-sdk';

// Track a custom event
await LinkzlySDK.trackEvent('tutorial_completed', { step: 5 });

// Example with a parameter
await LinkzlySDK.trackEvent('signup_completed', { method: 'email' });
```

### 4. Batch Tracking

Track multiple events efficiently in a single call.

```typescript
import LinkzlySDK from '@linkzly/react-native-sdk';

// Track multiple events
await LinkzlySDK.trackEventBatch([
  { eventName: 'screen_view', parameters: { screen: 'home' } },
  { eventName: 'button_click', parameters: { button: 'signup' } }
]);
```

## Event Queue Management

The SDK automatically queues events and sends them in batches for optimal network performance. You can also manually control the event queue.

```typescript
import LinkzlySDK from '@linkzly/react-native-sdk';

// Manually flush all pending events to the server
const success = await LinkzlySDK.flushEvents();
if (success) {
  console.log('All pending events sent');
}

// Check how many events are waiting to be sent
const count = await LinkzlySDK.getPendingEventCount();
console.log(`${count} events pending`);
```

**When to flush manually:**
- Before critical app transitions (e.g., logout)
- When you want to ensure events are sent before app termination
- During testing and debugging

## Debug and Batching Controls

These controls do not work on iOS: on iOS every `LinkzlyDebug` batching call rejects with the error code `DEBUG_ONLY`, because the published iOS SDK does not include the debug class. On Android they work in debuggable app builds only; in a release build they reject with `DEBUG_ONLY` too.

```typescript
import { LinkzlyDebug } from '@linkzly/react-native-sdk';
import type { BatchingStrategy } from '@linkzly/react-native-sdk';

export async function tryBatching(): Promise<void> {
  const strategy: BatchingStrategy = 'all';
  await LinkzlyDebug.setBatchingStrategy(strategy);
  await LinkzlyDebug.setBatchSize(5);
  await LinkzlyDebug.setFlushInterval(60);
  await LinkzlyDebug.simulateServerConfig({ batchSize: 10, flushInterval: 60, strategy: 'smart' });
  await LinkzlyDebug.printDebugConfig();
  const config = await LinkzlyDebug.getDebugConfig(); // { strategy?, batchSize?, flushInterval? } | null
  console.log(config);
  await LinkzlyDebug.resetDebugConfig();
}
```

`BatchingStrategy` values: `'all'` (batch all events), `'smart'` (batch only custom events; the default), `'instant'` (send every event immediately), `'custom'` (same as `'smart'`). `setBatchSize` takes 1 to 100; `setFlushInterval` takes 10 to 300 seconds. `LinkzlyDebug` also has `getPendingEventCount()` and `flushEvents()`.

## User Identification

```typescript
// Set user ID after authentication
await LinkzlySDK.setUserID('user_12345');

// Get current user ID
const userId = await LinkzlySDK.getUserID();

// Get persistent visitor ID
const visitorId = await LinkzlySDK.getVisitorID();

// Reset visitor ID
await LinkzlySDK.resetVisitorID();
```

## Affiliate Attribution Tracking

The SDK provides affiliate attribution tracking to capture affiliate click IDs from deep links and store them securely for server-to-server (S2S) conversion tracking. All affiliate methods delegate to the native iOS/Android SDKs for consistent behavior and secure persistent storage.

### How It Works

Affiliate attribution is **not captured automatically** by the SDK. Your app must call `captureAffiliateAttribution(url)` when handling a deep link that may contain affiliate parameters (`lz_click_id`, `lz_program_id`, `lz_affiliate_id`).

When you call `captureAffiliateAttribution(url)`:
1. The URL is passed to the native SDK via the bridge
2. The native SDK extracts affiliate parameters and stores them securely
3. The JS layer syncs a local cache for fast access

### Capture from Deep Link Handler

```typescript
import LinkzlySDK from '@linkzly/react-native-sdk';

// In your deep link handler — call captureAffiliateAttribution explicitly
const unsubscribe = LinkzlySDK.addUniversalLinkListener(async (data) => {
  // Capture affiliate attribution if present
  const captured = await LinkzlySDK.captureAffiliateAttribution(data.url);
  if (captured) {
    console.log('Affiliate attribution captured');
  }

  // Continue with normal deep link handling...
});
```

### Affiliate Attribution API

```typescript
import LinkzlySDK from '@linkzly/react-native-sdk';

// Capture affiliate attribution from a deep link URL
const captured = await LinkzlySDK.captureAffiliateAttribution(
  'https://example.com?lz_click_id=abc123&lz_program_id=prog456'
);

// Get the full attribution object
const attribution = await LinkzlySDK.getAffiliateAttribution();
// {
//   clickId: 'abc123',
//   programId: 'prog456',
//   affiliateId: 'aff789',
//   timestamp: 1234567890,
//   hasAttribution: true,
//   source: 'stored'
// }

// Get just the click ID for S2S conversion tracking
const clickId = await LinkzlySDK.getAffiliateClickId();

// Check if valid attribution exists (not expired)
const hasAttribution = await LinkzlySDK.hasAffiliateAttribution();

// Clear stored attribution data
await LinkzlySDK.clearAffiliateAttribution();
```

### Server-to-Server Conversion Tracking

Affiliate click IDs are **NOT automatically sent** with tracking events. This is intentional for S2S conversion tracking:

```typescript
// At checkout/conversion point:
const clickId = await LinkzlySDK.getAffiliateClickId();

if (clickId) {
  // Send to your backend for S2S conversion reporting
  await fetch('https://your-backend.com/api/conversions', {
    method: 'POST',
    body: JSON.stringify({
      click_id: clickId,
      order_value: 99.99,
      currency: 'USD'
    })
  });
}
```

### Attribution Expiry & Storage

- Attribution data automatically expires after **30 days** from capture
- **iOS**: Stored securely in Keychain (with UserDefaults fallback)
- **Android**: Stored in EncryptedSharedPreferences with AES256-GCM encryption
- **React Native**: All operations delegate to native — JS layer maintains an in-memory cache only
- If native SDKs are unavailable, falls back to in-memory storage with a warning

## Push Notification Support

The SDK exposes **two push features**. They are not independent: broadcast delivery is unlocked by device token registration, so start with the first one.

| Feature | Methods | What it does | When to use |
|---|---|---|---|
| **Device token registration** | `setNotificationToken` / `getNotificationToken` / `hasNotificationToken` / `clearNotificationToken` | Registers this device's **Firebase Cloud Messaging (FCM) registration token** in Linkzly's device registry so campaigns can target the specific device/user. On iOS as well as Android, pass the token your Firebase Messaging library gives you, never the APNs device token. Devices registered through the SDK are reached through Firebase Cloud Messaging. | You want Linkzly to send (or target) notifications to individual devices/users. |
| **Broadcast topic subscription** | `initializePush` / `disablePush` | Subscribes the device to its **per-(Smart App, platform) FCM topic** for "send to All" campaigns. The topic name comes from a successful `setNotificationToken` registration — `initializePush()` alone is a no-op until then. FCM-only, via runtime reflection. | You need broadcast-to-everyone campaigns and use Firebase Cloud Messaging. |

Before `configure` has run, `setNotificationToken` is ignored on both iOS and Android (nothing is stored). If your app delays `configure`, for example until the user has consented, call `setNotificationToken` again after `configure` with the current token from your Firebase Messaging library; a call made after `configure` registers normally.

**Registering a token is the whole setup.** A successful `setNotificationToken()` subscribes the device to its broadcast topic immediately, and the native SDK re-subscribes on every launch. `initializePush()` / `disablePush()` are recovery and opt-out controls, not the mechanism.

> **Native SDK versions.** Push lives in the native iOS/Android SDKs. This package already pins versions that support it — iOS `LinkzlySDK` 1.0.7+ and Android `linkzly-android-sdk` 1.0.9+ — so there is nothing extra to add.
>
> If the build can't find these methods, or broadcasts never arrive, your project is probably still resolving an older native SDK from a lockfile or cache:
>
> ```bash
> cd ios && pod update LinkzlySDK && cd ..
> cd android && ./gradlew --refresh-dependencies && cd ..
> ```

### Registering a device push token

`setNotificationToken` records the device's push token in Linkzly's device registry. Capture the token from your push library (e.g. `@react-native-firebase/messaging`) and forward it — the native SDK throttles network calls (it only re-registers when the token, user, or app version changes, or after 7 days), so it is safe to call on every launch.

```typescript
import messaging from '@react-native-firebase/messaging';
import LinkzlySDK from '@linkzly/react-native-sdk';

// On startup and whenever the token refreshes
const token = await messaging().getToken();
await LinkzlySDK.setNotificationToken(token);

messaging().onTokenRefresh(async (newToken) => {
  await LinkzlySDK.setNotificationToken(newToken);
});
```

**Binding to a user:** when you call `LinkzlySDK.setUserID(...)`, the native SDK automatically re-registers the stored token against the new user id, so campaigns can target that user.

**On logout / notifications disabled:** clear the token. This removes it locally and revokes it server-side.

```typescript
await LinkzlySDK.clearNotificationToken();
```

**Inspecting state:**

```typescript
const token = await LinkzlySDK.getNotificationToken(); // string | null
const has = await LinkzlySDK.hasNotificationToken();    // boolean
```

### Broadcast topic subscription (FCM-only)

Relevant if you want "send to All" broadcast campaigns and your app uses Firebase Cloud Messaging.

#### Prerequisites

- Firebase Cloud Messaging integrated in your React Native app (e.g., `@react-native-firebase/messaging`)
- Firebase live **before** you call `setNotificationToken` — see the ordering note below
- Linkzly SDK configured, and a successful `setNotificationToken()` registration

> **Note:** `initializePush()` and `disablePush()` are **Firebase Cloud Messaging only**. They subscribe/unsubscribe the device to its server-assigned FCM broadcast topic (`linkzly_broadcast_<smartAppId>_<platform>`) using runtime reflection. **Broadcast reach requires a successful `setNotificationToken()` registration first** — the topic name is returned by `/api/sdk/devices/register`. Calling `initializePush()` alone does not receive broadcasts.
>
> If your app also uses another push provider for its own delivery, you do **not** need to call these methods for it. Devices registered through the SDK are reached through Firebase Cloud Messaging.

#### Setup

There is no separate broadcast setup call. Register the token and the subscribe happens for you — just make sure Firebase is live first:

```typescript
import messaging from '@react-native-firebase/messaging';
import LinkzlySDK from '@linkzly/react-native-sdk';

// 1. Firebase must be live before the token is registered, or the topic is
//    stored but the subscribe fails. @react-native-firebase auto-initializes
//    the default app from google-services.json / GoogleService-Info.plist, so
//    there is usually nothing to call here — but if you initialize Firebase
//    manually, do it before step 4.
// 2. Configure Linkzly
await LinkzlySDK.configure('YOUR_SDK_KEY');

// 3. Ask for permission, then forward the token.
await messaging().requestPermission();
const token = await messaging().getToken();

// 4. This is what enables broadcasts: registration stores the topic and
//    subscribes to it.
await LinkzlySDK.setNotificationToken(token);
```

`initializePush()` re-subscribes to the topic stored by the last successful `setNotificationToken()` registration. That makes it a **recovery call** for two situations: you unsubscribed from the topic yourself, or registration succeeded while Firebase Messaging was not yet live so the topic was stored but the subscribe failed. If registration has never succeeded there is no stored topic and it returns `false` — fix the registration instead.

```typescript
const resubscribed = await LinkzlySDK.initializePush();
```

#### Disabling Push

```typescript
await LinkzlySDK.disablePush();
```

> **`disablePush()` is session-scoped, not a durable opt-out.** It unsubscribes, but the stored topic and token are kept, so the next launch re-subscribes. For a permanent opt-out use `clearNotificationToken()` — it revokes the token server-side and clears the stored topic.

#### How It Works

- A successful `setNotificationToken()` registration stores the device's topic (`linkzly_broadcast_<smartAppId>_<platform>`) and subscribes to it
- The native SDK re-subscribes on every launch, so a deferred or lost subscribe self-heals
- Topics are scoped per Smart App and platform, so a campaign only reaches the app it selected — the old shared `linkzly_broadcast` topic is unsubscribed automatically
- `initializePush()` re-subscribes to the stored topic; it does nothing until one exists
- No Firebase dependency is added to the Linkzly SDK itself — it uses runtime reflection
- If Firebase Messaging is not available, the methods return `false` safely (no crash)

#### Troubleshooting

| Symptom | Likely cause |
|---|---|
| `initializePush()` returns `false` | No topic stored yet — `setNotificationToken()` has not completed successfully. Or Firebase Messaging is not in your app. |
| Broadcasts never arrive | The device never registered. Check that `setNotificationToken()` was called with a real FCM registration token (on iOS the Firebase token, not the APNs device token) and that the registry call succeeded. |
| Broadcasts arrive only after a restart | Firebase was not live when the token registered, so the topic was stored but the subscribe failed. Initialize Firebase earlier, or call `initializePush()` once Firebase is ready. |
| Notifications stop after a "disable" | `disablePush()` only lasts the session. Use `clearNotificationToken()` for a durable opt-out. |
| Wrong app receives a campaign | The device is still on the legacy shared topic — it is dropped on the next successful registration against a current native SDK. |

## Gaming Intelligence

The Gaming Intelligence module provides high-performance batch event tracking designed for games. It includes automatic session management, event queuing with retry logic, and optional request signing.

### Configuration

```typescript
import LinkzlySDK, { Environment } from '@linkzly/react-native-sdk';

// Basic configuration
await LinkzlySDK.configureGamingTracking(
  'your_gaming_api_key',
  'your_org_id',
  'your_game_id',
  Environment.PRODUCTION
);

// Advanced configuration with options
await LinkzlySDK.configureGamingTracking(
  'your_gaming_api_key',
  'your_org_id',
  'your_game_id',
  Environment.PRODUCTION,
  {
    gameVersion: '1.2.0',
    maxBatchSize: 50,
    flushIntervalMs: 10000,  // 10 seconds
    debug: true,
  }
);
```

### Player Identification

```typescript
// Identify the current player
await LinkzlySDK.identifyGamingPlayer('player_12345', {
  level: 42,
  vip_tier: 'gold',
  registration_date: '2024-01-15',
});

// Reset player identification (e.g., on logout)
await LinkzlySDK.resetGamingTracking();
```

### Session Management

Sessions are tracked automatically by default. For manual control:

```typescript
await LinkzlySDK.startGamingSession();
await LinkzlySDK.endGamingSession();
```

### Event Tracking

```typescript
// Track a gaming event (batched and sent automatically)
await LinkzlySDK.trackGamingEvent('level_complete', {
  level: 5,
  score: 12500,
  time_seconds: 120,
  stars: 3,
});

// Track a high-priority event (sent immediately, bypasses batching)
await LinkzlySDK.trackGamingEvent('purchase', {
  item_id: 'sword_of_fire',
  price: 4.99,
  currency: 'USD',
}, true);  // immediate = true
```

A gaming `purchase` event is game telemetry and is not recorded as revenue; report revenue with `trackPurchase`.

### Attribution

Link game installs and opens to marketing campaigns:

```typescript
// Set attribution data (e.g., from a deep link or install referrer)
await LinkzlySDK.setGamingAttribution(
  'click_abc123',                         // clickId
  'https://yourgame.com/promo',           // deferredDeepLink
  { campaign: 'summer_sale' }             // metadata
);

// Clear attribution
await LinkzlySDK.clearGamingAttribution();
```

### Queue Management

```typescript
// Manually flush queued gaming events
await LinkzlySDK.flushGamingEvents();

// Check queue status
const status = await LinkzlySDK.getGamingStatus();
console.log('Pending events:', status.pendingEventCount);
console.log('Batch in flight:', status.hasInflightBatch);

// Reset all gaming tracking state (clears queue, player ID, attribution)
await LinkzlySDK.resetGamingTracking();
```

### Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `gameVersion` | `""` | Your game's version string |
| `maxBatchSize` | `100` | Maximum events per batch |
| `maxBatchBytes` | `512 KB` | Maximum batch size in bytes |
| `flushIntervalMs` | `5,000` | Auto-flush interval in milliseconds |
| `maxRetries` | `3` | Maximum retry attempts per batch |
| `retryDelayMs` | `1,000` | Delay between retries in milliseconds |
| `maxQueueSize` | `10,000` | Maximum events in queue |
| `sessionTimeoutMs` | `1,800,000` | Session timeout (30 minutes) |
| `autoSessionTracking` | `true` | Enable automatic session management |
| `signingSecret` | `null` | Optional HMAC signing secret |
| `debug` | `false` | Enable debug logging |

## Privacy Controls

```typescript
// Stop custom events, purchases and refunds
// (not install, open or session-end events; on iOS trackEventBatch is not stopped;
// on Android no effect before configure())
await LinkzlySDK.setTrackingEnabled(false);
const isEnabled = await LinkzlySDK.isTrackingEnabled();

// Enable/disable advertising ID collection (IDFA/GAID)
await LinkzlySDK.setAdvertisingTrackingEnabled(true);
const isAdEnabled = await LinkzlySDK.isAdvertisingTrackingEnabled();
```

`setTrackingEnabled(false)` stops custom events, purchases and refunds, and on Android `trackEventBatch`; on iOS `trackEventBatch` is not stopped. It does not stop the install, open and session-end events the SDK sends itself, and on Android it has no effect before `configure`. The SDK sends nothing before `configure`: an app that must send nothing before the user consents delays `configure` until the user has consented, and uses `setTrackingEnabled(false)` afterwards to stop events, purchases and refunds.

## Advertising Identifiers (IDFA/GAID)

**iOS - Request ATT Permission:**

```typescript
import { Platform } from 'react-native';

if (Platform.OS === 'ios') {
  const status = await LinkzlySDK.requestTrackingPermission();
  // Returns: 'authorized' | 'denied' | 'restricted' | 'notDetermined'

  const idfa = await LinkzlySDK.getIDFA();
  const attStatus = await LinkzlySDK.getATTStatus();
}
```

**Required `Info.plist` entry:**

```xml
<key>NSUserTrackingUsageDescription</key>
<string>We use this to provide personalized content and measure ad effectiveness.</string>
```

## SKAdNetwork Support (iOS 14+)

```typescript
// Update conversion value (0-63)
await LinkzlySDK.updateConversionValue(42);
```

## Environments

```typescript
// Development (logging enabled)
LinkzlySDK.configure('dev_key', Environment.DEVELOPMENT);

// Staging
LinkzlySDK.configure('staging_key', Environment.STAGING);

// Production (default)
LinkzlySDK.configure('prod_key', Environment.PRODUCTION);
```

## Verify Your Setup

After integration, verify the SDK is working correctly:

**1. Check SDK Initialization:**
```typescript
LinkzlySDK.configure('YOUR_SDK_KEY', Environment.DEVELOPMENT);
// Check console for: "LinkzlySDK configured successfully"
```

**2. Verify Event Tracking:**

Register `test_event` for the app first (see [Register each name in the console](https://docs.linkzly.com/docs/sdk-tracking-events#register-each-name-in-the-console)). Linkzly drops an event whose name is not registered.

```typescript
await LinkzlySDK.trackEvent('test_event', { test: true });
// The SDK log only shows that the event was sent. An unregistered name is dropped by Linkzly.
```

**3. Test Deep Links:**

**iOS (Simulator):**
```bash
xcrun simctl openurl booted "https://yourdomain.com/product?id=123"
# Or with custom scheme:
xcrun simctl openurl booted "yourapp://product?id=123"
```

**Android (Device/Emulator):**
```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "https://yourdomain.com/product?id=123" \
  com.yourcompany.yourapp
```

**4. Verify Deep Link Listener:**
```typescript
LinkzlySDK.addDeepLinkListener((data) => {
  console.log('Deep link received:', data);
  // Should see path, parameters, smartLinkId, etc.
});
```

**Setup Verification Checklist:**

**JavaScript/TypeScript:**
- [ ] SDK package installed (`@linkzly/react-native-sdk`)
- [ ] SDK imports without errors
- [ ] SDK initializes successfully (check console)
- [ ] Deep link listener configured
- [ ] Registered `test_event` for the app and sent it (the SDK log only shows the event was sent; an unregistered name is dropped by Linkzly)

**iOS:**
- [ ] CocoaPods dependencies installed (`pod install`)
- [ ] LinkzlySDK native dependency configured
- [ ] `AppDelegate.swift` handles Universal Links
- [ ] Associated Domains capability added in Xcode
- [ ] Info.plist has URL schemes configured
- [ ] Deep links open the app (test with xcrun)

**Android:**
- [ ] Android build succeeds (the package links the native SDK itself)
- [ ] Intent filters configured in AndroidManifest
- [ ] `MainActivity` handles deep links in `onNewIntent`
- [ ] `launchMode="singleTask"` set on MainActivity
- [ ] Deep links open the app (test with adb)

**Expected Console Output:**

When running in `Environment.DEVELOPMENT`, you should see:
```
[LinkzlySDK] Configuring SDK...
[LinkzlySDK] SDK configured successfully
[LinkzlySDK] Tracking install event (first launch)
[LinkzlySDK] Event tracked: install
[LinkzlySDK] Deep link received: https://yourdomain.com/product?id=123
[LinkzlySDK] Deep link data: {"path":"/product","parameters":{"id":"123"}}
```

## API Reference

### Configuration

| Method | Returns | Description |
|--------|---------|-------------|
| `configure(sdkKey, environment?, options?)` | `Promise<void>` | Initialize the SDK |
| `setAutoHandleDeepLinks(enabled)` | `void` | Enable/disable automatic deep link handling |
| `isAutoHandleDeepLinksEnabled()` | `boolean` | Check if auto deep link handling is enabled |

### Attribution Tracking

| Method | Returns | Description |
|--------|---------|-------------|
| `trackInstall()` | `Promise<DeepLinkData \| null>` | Track app install. The SDK already sends the install on a genuine first launch, so calling this on a new install sends a second install event. On iOS use `addDeepLinkListener` for the deferred data; on Android this call is the only way to get it |
| `trackOpen()` | `Promise<DeepLinkData \| null>` | Track app open. The SDK already sends opens itself; an explicit call sends an additional open event |
| `trackEvent(eventName, parameters?)` | `Promise<void>` | Track a custom event |
| `trackPurchase(parameters?)` | `Promise<void>` | Track a purchase (`revenue`, `currency`, `transactionId` required) |
| `trackRefund(parameters?)` | `Promise<void>` | Track a refund (reuse the original `transactionId`) |
| `trackEventBatch(events)` | `Promise<boolean>` | Track multiple events in a batch |

### Deep Link Handling

| Method | Returns | Description |
|--------|---------|-------------|
| `handleUniversalLink(url)` | `Promise<DeepLinkData \| null>` | Handle a universal/app link |
| `addDeepLinkListener(listener)` | `() => void` | Add deep link listener (returns unsubscribe) |
| `addUniversalLinkListener(listener)` | `() => void` | Add universal link listener (returns unsubscribe) |
| `removeAllListeners()` | `void` | Remove all event listeners |

### Event Queue

| Method | Returns | Description |
|--------|---------|-------------|
| `flushEvents()` | `Promise<boolean>` | Manually flush pending events |
| `getPendingEventCount()` | `Promise<number>` | Get count of queued events |

### User Management

| Method | Returns | Description |
|--------|---------|-------------|
| `setUserID(userID)` | `Promise<void>` | Set user ID for attribution |
| `getUserID()` | `Promise<string \| null>` | Get current user ID |
| `getVisitorID()` | `Promise<string>` | Get persistent visitor ID |
| `resetVisitorID()` | `Promise<void>` | Reset visitor ID (generates new) |

### Privacy

| Method | Returns | Description |
|--------|---------|-------------|
| `setTrackingEnabled(enabled)` | `Promise<void>` | Stops or resumes custom events, purchases and refunds. Install, open and session-end events the SDK sends itself are not affected; on iOS `trackEventBatch` is not stopped; on Android it has no effect before `configure` |
| `isTrackingEnabled()` | `Promise<boolean>` | Check if tracking is enabled |
| `setAdvertisingTrackingEnabled(enabled)` | `Promise<void>` | Enable/disable IDFA/GAID collection |
| `isAdvertisingTrackingEnabled()` | `Promise<boolean>` | Check if ad tracking is enabled |

### iOS Only

| Method | Returns | Description |
|--------|---------|-------------|
| `requestTrackingPermission()` | `Promise<string>` | Request ATT permission (iOS 14.5+); resolves `'unsupported'` on Android |
| `getIDFA()` | `Promise<string \| null>` | Get current IDFA value |
| `getATTStatus()` | `Promise<string \| null>` | Get ATT authorization status |
| `updateConversionValue(value)` | `Promise<boolean>` | Update SKAdNetwork conversion value (0-63); resolves `false` on Android |

### Affiliate Attribution

| Method | Returns | Description |
|--------|---------|-------------|
| `captureAffiliateAttribution(url)` | `Promise<boolean>` | Capture affiliate attribution from URL |
| `getAffiliateAttribution()` | `Promise<AffiliateAttribution>` | Get full attribution object |
| `getAffiliateClickId()` | `Promise<string \| null>` | Get click ID for S2S tracking |
| `hasAffiliateAttribution()` | `Promise<boolean>` | Check if valid attribution exists |
| `clearAffiliateAttribution()` | `Promise<void>` | Clear stored attribution data |

### Push Notifications

| Method | Returns | Description |
|--------|---------|-------------|
| `setNotificationToken(token)` | `Promise<void>` | Register this device's FCM registration token in Linkzly's device registry (throttled); on iOS the Firebase token, not the APNs device token |
| `getNotificationToken()` | `Promise<string \| null>` | Get the currently stored push token |
| `hasNotificationToken()` | `Promise<boolean>` | Whether a push token is stored |
| `clearNotificationToken()` | `Promise<void>` | Clear the token locally and revoke it server-side (logout) |
| `initializePush()` | `Promise<boolean>` | Re-subscribe to the stored broadcast topic (recovery; no-op before registration) |
| `disablePush()` | `Promise<boolean>` | Unsubscribe from the broadcast topic for this session (not a durable opt-out) |

### Debug (`LinkzlyDebug`)

Rejects with `DEBUG_ONLY` on iOS; works in debuggable builds on Android.

| Method | Returns | Description |
|--------|---------|-------------|
| `setBatchingStrategy(strategy)` | `Promise<void>` | `'all'`, `'smart'`, `'instant'` or `'custom'` |
| `setBatchSize(size)` | `Promise<void>` | Events per batch (1-100) |
| `setFlushInterval(seconds)` | `Promise<void>` | Flush interval (10-300 seconds) |
| `simulateServerConfig(config)` | `Promise<void>` | Set batch size, flush interval and optional strategy at once |
| `resetDebugConfig()` | `Promise<void>` | Clear all debug overrides |
| `printDebugConfig()` | `Promise<void>` | Log the debug configuration to the native console |
| `getDebugConfig()` | `Promise<DebugBatchConfig \| null>` | Current debug configuration |
| `getPendingEventCount()` | `Promise<number>` | Queued events (same as on `LinkzlySDK`) |
| `flushEvents()` | `Promise<boolean>` | Send queued events now (same as on `LinkzlySDK`) |

### Gaming Intelligence

| Method | Returns | Description |
|--------|---------|-------------|
| `configureGamingTracking(apiKey, orgId, gameId, env?, options?)` | `Promise<void>` | Initialize gaming module |
| `identifyGamingPlayer(playerId, traits?)` | `Promise<void>` | Set player identity |
| `trackGamingEvent(eventType, data?, immediate?)` | `Promise<void>` | Track a gaming event |
| `startGamingSession()` | `Promise<void>` | Start a gaming session |
| `endGamingSession()` | `Promise<void>` | End the current gaming session |
| `flushGamingEvents()` | `Promise<void>` | Flush queued gaming events |
| `setGamingAttribution(clickId?, deepLink?, metadata?)` | `Promise<void>` | Set gaming attribution data |
| `clearGamingAttribution()` | `Promise<void>` | Clear gaming attribution |
| `resetGamingTracking()` | `Promise<void>` | Reset all gaming state |
| `getGamingStatus()` | `Promise<GamingTrackingStatus>` | Get queue status |

## Type Definitions

```typescript
enum Environment {
  PRODUCTION = 0,
  STAGING = 1,
  DEVELOPMENT = 2,
}

interface DeepLinkData {
  url?: string;
  path?: string;
  parameters: Record<string, any>;
  smartLinkId?: string;
  clickId?: string;
}

interface UniversalLinkEvent {
  url: string;
  path?: string;
  parameters: Record<string, any>;
  attributionData?: Record<string, any>;
}

interface EventParameters {
  [key: string]: string | number | boolean | any;
}

interface BatchEvent {
  eventName: string;
  parameters?: EventParameters;
}

interface AffiliateAttribution {
  clickId: string | null;
  programId: string | null;
  affiliateId: string | null;
  timestamp: number | null;
  hasAttribution: boolean;
  source: 'deep_link' | 'stored' | 'none';
}

type BatchingStrategy = 'all' | 'smart' | 'instant' | 'custom';

interface DebugBatchConfig {
  strategy?: string;
  batchSize?: number;
  flushInterval?: number;
}

interface GamingTrackingOptions {
  baseUrl?: string;
  endpointPath?: string;
  sdkVersion?: string;
  gameVersion?: string;
  includeTraits?: boolean;
  debug?: boolean;
  maxBatchSize?: number;
  maxBatchBytes?: number;
  flushIntervalMs?: number;
  maxRetries?: number;
  retryDelayMs?: number;
  maxQueueSize?: number;
  sessionTimeoutMs?: number;
  autoSessionTracking?: boolean;
  signingSecret?: string;
}

interface GamingTrackingStatus {
  pendingEventCount: number;
  hasInflightBatch: boolean;
}
```

## Testing Deep Links

### iOS Simulator

```bash
xcrun simctl openurl booted "https://yourdomain.com/product?id=123"
```

### Android Emulator

```bash
adb shell am start -a android.intent.action.VIEW \
  -d "https://yourdomain.com/product?id=123" \
  com.yourapp
```

## Example App

Explore a complete working example in the `example/` directory:

```bash
cd example
npm install
cd ios && pod install && cd ..

npm run ios     # Run iOS
npm run android # Run Android
```

The example pins an older SDK release in its `package.json` and `Podfile`. Take install lines and versions from this README, not from the example.

## Troubleshooting

### Deep Links Not Working on Warm Start (Background → Foreground)

**iOS:**
- Ensure you've added both `RCTLinkingManager` calls in your AppDelegate (see [iOS Deep Link Configuration](#ios-deep-link-configuration-required-for-warm-start))
- After modifying AppDelegate, you must **rebuild the iOS app** (`pod install` + rebuild from Xcode)
- Verify both URL schemes and Universal Links are configured in `Info.plist` and Entitlements

**Android:**
- Ensure `onNewIntent()` is overridden in MainActivity (see [Android Deep Link Configuration](#android-deep-link-configuration-required-for-warm-start))
- Verify `android:launchMode="singleTask"` is set in `AndroidManifest.xml`
- After modifying native code, rebuild the Android app

### Deep Link Listener Not Firing

```typescript
// Ensure listener is set up BEFORE configure()
const unsubscribe = LinkzlySDK.addDeepLinkListener((data) => {
  console.log('Deep link:', data);
});

await LinkzlySDK.configure('YOUR_SDK_KEY', Environment.PRODUCTION);

// Don't forget to unsubscribe on cleanup
return () => unsubscribe();
```

## License

MIT

## Support

- 📧 Email: support@linkzly.com
- 📚 Documentation: https://docs.linkzly.com/docs/sdk-react-native
- 🐛 Issues: https://github.com/Linkzly/react-native-linkzly-sdk/issues
