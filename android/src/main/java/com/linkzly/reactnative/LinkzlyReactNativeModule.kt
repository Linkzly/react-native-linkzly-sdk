package com.linkzly.reactnative

import android.content.Intent
import android.net.Uri
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.linkzly.sdk.LinkzlySDK
import com.linkzly.sdk.gaming.LinkzlyGamingTracking
import com.linkzly.sdk.models.DeepLinkData
import com.linkzly.sdk.models.Environment
import com.linkzly.sdk.utils.LinkzlySDKDebug

class LinkzlyReactNativeModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private val reactAppContext: ReactApplicationContext = reactContext

    init {
        latestInstance = this
    }

    override fun getName(): String {
        return "LinkzlyReactNative"
    }

    private fun sendEvent(eventName: String, params: WritableMap?) {
        reactAppContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    @ReactMethod
    fun configure(sdkKey: String, environment: Int, promise: Promise) {
        try {
            val env = when (environment) {
                0 -> Environment.PRODUCTION
                1 -> Environment.STAGING
                2 -> Environment.DEVELOPMENT
                else -> Environment.PRODUCTION
            }

            LinkzlySDK.configure(reactAppContext, sdkKey, env)

            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("CONFIGURE_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun handleAppLink(urlString: String, promise: Promise) {
        try {
            val intent = Intent().apply {
                data = android.net.Uri.parse(urlString)
            }

            val deepLinkData = LinkzlySDK.handleAppLink(intent)

            if (deepLinkData != null) {
                val eventData = Arguments.createMap().apply {
                    deepLinkData.url?.let { putString("url", it) }
                    putString("path", deepLinkData.path)
                    putMap("parameters", convertMapToWritableMap(deepLinkData.parameters))
                    putString("smartLinkId", deepLinkData.smartLinkId)
                    putString("clickId", deepLinkData.clickId)
                }
                sendEvent("LinkzlyDeepLinkReceived", eventData)
                promise.resolve(eventData)
            } else {
                promise.resolve(null)
            }
        } catch (e: Exception) {
            promise.reject("HANDLE_LINK_ERROR", e.message, e)
        }
    }

    /**
     * Handle deep link intent directly from MainActivity.onNewIntent()
     * This bypasses React Native's unreliable Linking.addEventListener on Android warm start
     * @param intent The intent containing the deep link URL
     * @return true if the intent was handled, false otherwise
     */
    fun handleIntent(intent: Intent?): Boolean {
        if (intent?.data == null) {
            return false
        }

        val urlString = intent.data.toString()
        android.util.Log.d("LinkzlyReactNative", "handleIntent called with URL: $urlString")

        try {
            // Process through native Linkzly SDK
            val deepLinkData = LinkzlySDK.handleAppLink(intent)

            // Emit event to React Native
            if (deepLinkData != null) {
                val eventData = Arguments.createMap().apply {
                    deepLinkData.url?.let { putString("url", it) }
                    putString("path", deepLinkData.path)
                    putMap("parameters", convertMapToWritableMap(deepLinkData.parameters))
                    putString("smartLinkId", deepLinkData.smartLinkId)
                    putString("clickId", deepLinkData.clickId)
                }
                sendEvent("LinkzlyDeepLinkReceived", eventData)
                android.util.Log.d("LinkzlyReactNative", "Deep link event emitted successfully")
                return true
            }
        } catch (e: Exception) {
            android.util.Log.e("LinkzlyReactNative", "Error handling intent: ${e.message}", e)
        }

        return false
    }

    @ReactMethod
    fun trackInstall(promise: Promise) {
        try {
            LinkzlySDK.trackInstall { result ->
                result.fold(
                    onSuccess = { deepLinkData ->
                        if (deepLinkData != null) {
                            val data = Arguments.createMap().apply {
                                deepLinkData.url?.let { putString("url", it) }
                                putString("path", deepLinkData.path)
                                putMap("parameters", convertMapToWritableMap(deepLinkData.parameters))
                                putString("smartLinkId", deepLinkData.smartLinkId)
                                putString("clickId", deepLinkData.clickId)
                            }
                            promise.resolve(data)
                        } else {
                            promise.resolve(null)
                        }
                    },
                    onFailure = { error ->
                        promise.reject("TRACK_INSTALL_ERROR", error.message, error)
                    }
                )
            }
        } catch (e: Exception) {
            promise.reject("TRACK_INSTALL_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun trackOpen(promise: Promise) {
        try {
            LinkzlySDK.trackOpen { result ->
                result.fold(
                    onSuccess = { deepLinkData ->
                        if (deepLinkData != null) {
                            val data = Arguments.createMap().apply {
                                deepLinkData.url?.let { putString("url", it) }
                                putString("path", deepLinkData.path)
                                putMap("parameters", convertMapToWritableMap(deepLinkData.parameters))
                                putString("smartLinkId", deepLinkData.smartLinkId)
                                putString("clickId", deepLinkData.clickId)
                            }
                            promise.resolve(data)
                        } else {
                            promise.resolve(null)
                        }
                    },
                    onFailure = { error ->
                        promise.reject("TRACK_OPEN_ERROR", error.message, error)
                    }
                )
            }
        } catch (e: Exception) {
            promise.reject("TRACK_OPEN_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun trackEvent(eventName: String, parameters: ReadableMap?, promise: Promise) {
        try {
            val params = parameters?.toHashMap()?.mapValues { it.value ?: "" }?.map { it.key to it.value }?.toMap() ?: emptyMap<String, Any>()
            LinkzlySDK.trackEvent(eventName, params)

            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("TRACK_EVENT_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun trackPurchase(parameters: ReadableMap?, promise: Promise) {
        try {
            val params = parameters?.toHashMap()?.mapValues { it.value ?: "" }?.map { it.key to it.value }?.toMap() ?: emptyMap<String, Any>()

            LinkzlySDK.trackPurchase(params) { result ->
                result.fold(
                    onSuccess = { success ->
                        val response = Arguments.createMap()
                        response.putBoolean("success", success)
                        promise.resolve(response)
                    },
                    onFailure = { error ->
                        promise.reject("TRACK_PURCHASE_ERROR", error.message, error)
                    }
                )
            }
        } catch (e: Exception) {
            promise.reject("TRACK_PURCHASE_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun trackEventBatch(events: ReadableArray, promise: Promise) {
        try {
            val eventList = mutableListOf<Map<String, Any>>()

            for (i in 0 until events.size()) {
                events.getMap(i)?.toHashMap()?.mapValues { it.value ?: "" }?.map { it.key to it.value }?.toMap()?.let { eventList.add(it) }
            }

            LinkzlySDK.trackEventBatch(eventList) { result ->
                result.fold(
                    onSuccess = { success ->
                        val response = Arguments.createMap()
                        response.putBoolean("success", success)
                        promise.resolve(response)
                    },
                    onFailure = { error ->
                        promise.reject("TRACK_BATCH_ERROR", error.message, error)
                    }
                )
            }
        } catch (e: Exception) {
            promise.reject("TRACK_BATCH_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun setUserID(userID: String, promise: Promise) {
        try {
            LinkzlySDK.setUserID(userID)

            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("SET_USER_ID_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getUserID(promise: Promise) {
        try {
            val userID = LinkzlySDK.getUserID()
            promise.resolve(userID)
        } catch (e: Exception) {
            promise.reject("GET_USER_ID_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun setTrackingEnabled(enabled: Boolean, promise: Promise) {
        try {
            LinkzlySDK.setTrackingEnabled(enabled)

            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("SET_TRACKING_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun isTrackingEnabled(promise: Promise) {
        try {
            val enabled = LinkzlySDK.isTrackingEnabled()
            promise.resolve(enabled)
        } catch (e: Exception) {
            promise.reject("GET_TRACKING_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getVisitorID(promise: Promise) {
        try {
            val visitorID = LinkzlySDK.getVisitorID()
            promise.resolve(visitorID)
        } catch (e: Exception) {
            promise.reject("GET_VISITOR_ID_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun resetVisitorID(promise: Promise) {
        try {
            LinkzlySDK.resetVisitorID()

            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("RESET_VISITOR_ID_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun updateConversionValue(value: Int, promise: Promise) {
        // SKAdNetwork is iOS only, not applicable on Android
        val result = Arguments.createMap()
        result.putBoolean("success", false)
        result.putString("message", "SKAdNetwork is iOS only")
        promise.resolve(result)
    }

    @ReactMethod
    fun setAdvertisingTrackingEnabled(enabled: Boolean, promise: Promise) {
        try {
            LinkzlySDK.setAdvertisingTrackingEnabled(enabled)

            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("SET_ADVERTISING_TRACKING_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun isAdvertisingTrackingEnabled(promise: Promise) {
        try {
            val enabled = LinkzlySDK.isAdvertisingTrackingEnabled()
            promise.resolve(enabled)
        } catch (e: Exception) {
            promise.reject("GET_ADVERTISING_TRACKING_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun startSession(promise: Promise) {
        try {
            LinkzlySDK.startSession()
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("START_SESSION_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun endSession(promise: Promise) {
        try {
            LinkzlySDK.endSession()
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("END_SESSION_ERROR", e.message, e)
        }
    }

    // MARK: - Flush Events and Pending Count

    @ReactMethod
    fun flushEvents(promise: Promise) {
        try {
            LinkzlySDK.flushEvents { result ->
                result.fold(
                    onSuccess = { success ->
                        val response = Arguments.createMap()
                        response.putBoolean("success", success)
                        promise.resolve(response)
                    },
                    onFailure = { error ->
                        promise.reject("FLUSH_ERROR", error.message, error)
                    }
                )
            }
        } catch (e: Exception) {
            promise.reject("FLUSH_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getPendingEventCount(promise: Promise) {
        try {
            val count = LinkzlySDK.getPendingEventCount()
            promise.resolve(count)
        } catch (e: Exception) {
            promise.reject("GET_PENDING_COUNT_ERROR", e.message, e)
        }
    }

    // MARK: - Gaming Tracking (Additive)

    @ReactMethod
    fun configureGamingTracking(
        apiKey: String,
        organizationId: String,
        gameId: String,
        environment: Int,
        options: ReadableMap?,
        promise: Promise
    ) {
        try {
            val envBaseUrl = when (environment) {
                1 -> "https://linkzly-gaming-tracking-staging.mec-fahid.workers.dev"
                2 -> "https://linkzly-gaming-tracking-development.mec-fahid.workers.dev"
                else -> "https://gaming.linkzly.com"
            }

            val map = options?.toHashMap() ?: emptyMap<String, Any>()
            fun asInt(value: Any?, fallback: Int): Int {
                return when (value) {
                    is Number -> value.toInt()
                    is String -> value.toIntOrNull() ?: fallback
                    else -> fallback
                }
            }

            fun asBool(value: Any?, fallback: Boolean): Boolean {
                return when (value) {
                    is Boolean -> value
                    is Number -> value.toInt() != 0
                    is String -> when (value.lowercase()) {
                        "true", "1", "yes" -> true
                        "false", "0", "no" -> false
                        else -> fallback
                    }
                    else -> fallback
                }
            }

            val config = LinkzlyGamingTracking.GamingOptions(
                apiKey = apiKey,
                organizationId = organizationId,
                gameId = gameId,
                baseUrl = map["baseUrl"] as? String ?: envBaseUrl,
                endpointPath = map["endpointPath"] as? String ?: "/api/v1/gaming/events",
                sdkVersion = map["sdkVersion"] as? String ?: "1.0.0",
                gameVersion = map["gameVersion"] as? String ?: "",
                includeTraits = asBool(map["includeTraits"], false),
                debug = asBool(map["debug"], false),
                maxBatchSize = asInt(map["maxBatchSize"], 100),
                maxBatchBytes = asInt(map["maxBatchBytes"], 512 * 1024),
                flushIntervalMs = asInt(map["flushIntervalMs"], 5000),
                maxRetries = asInt(map["maxRetries"], 3),
                retryDelayMs = asInt(map["retryDelayMs"], 1000),
                maxQueueSize = asInt(map["maxQueueSize"], 10000),
                sessionTimeoutMs = asInt(map["sessionTimeoutMs"], 30 * 60 * 1000),
                autoSessionTracking = asBool(map["autoSessionTracking"], true),
                signingSecret = map["signingSecret"] as? String
            )

            LinkzlyGamingTracking.configure(reactAppContext, config)
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("GAMING_CONFIG_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun identifyGamingPlayer(playerId: String, traits: ReadableMap?, promise: Promise) {
        try {
            @Suppress("UNCHECKED_CAST")
            val traitMap = traits?.toHashMap() as? Map<String, Any>
            LinkzlyGamingTracking.identify(playerId, traitMap)
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("GAMING_IDENTIFY_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun trackGamingEvent(eventType: String, data: ReadableMap?, immediate: Boolean, promise: Promise) {
        try {
            @Suppress("UNCHECKED_CAST")
            val payload = (data?.toHashMap() as? Map<String, Any>) ?: emptyMap()
            LinkzlyGamingTracking.track(eventType, payload, immediateFlush = immediate)
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("GAMING_TRACK_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun flushGamingEvents(promise: Promise) {
        LinkzlyGamingTracking.flush(LinkzlyGamingTracking.LinkzlyGamingFlushReason.MANUAL_FLUSH) { success, error ->
            if (success) {
                val result = Arguments.createMap()
                result.putBoolean("success", true)
                promise.resolve(result)
            } else {
                promise.reject("GAMING_FLUSH_ERROR", error ?: "Gaming flush failed", null)
            }
        }
    }

    @ReactMethod
    fun startGamingSession(promise: Promise) {
        try {
            LinkzlyGamingTracking.startSession()
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("GAMING_SESSION_START_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun endGamingSession(promise: Promise) {
        try {
            LinkzlyGamingTracking.endSession()
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("GAMING_SESSION_END_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun setGamingAttribution(clickId: String?, deferredDeepLink: String?, metadata: ReadableMap?, promise: Promise) {
        try {
            @Suppress("UNCHECKED_CAST")
            val payload = metadata?.toHashMap() as? Map<String, Any>
            LinkzlyGamingTracking.setAttribution(clickId, deferredDeepLink, payload)
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("GAMING_ATTRIBUTION_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun clearGamingAttribution(promise: Promise) {
        try {
            LinkzlyGamingTracking.clearAttribution()
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("GAMING_ATTRIBUTION_CLEAR_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun resetGamingTracking(promise: Promise) {
        try {
            LinkzlyGamingTracking.reset()
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("GAMING_RESET_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getGamingStatus(promise: Promise) {
        try {
            val result = Arguments.createMap()
            result.putInt("pendingEventCount", LinkzlyGamingTracking.getPendingCount())
            result.putBoolean("hasInflightBatch", LinkzlyGamingTracking.hasInflightBatch())
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("GAMING_STATUS_ERROR", e.message, e)
        }
    }

    // ─── Push Notification Support ─────────────────────────────────

    @ReactMethod
    fun initializePush(promise: Promise) {
        try {
            val result = LinkzlySDK.initializePush()
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("INIT_PUSH_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun disablePush(promise: Promise) {
        try {
            val result = LinkzlySDK.disablePush()
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("DISABLE_PUSH_ERROR", e.message, e)
        }
    }

    // ─── Affiliate Attribution ─────────────────────────────────────

    @ReactMethod
    fun captureAffiliateAttribution(urlString: String, promise: Promise) {
        try {
            val uri = Uri.parse(urlString)
            val captured = LinkzlySDK.captureAffiliateAttribution(uri)
            promise.resolve(captured)
        } catch (e: Exception) {
            promise.reject("AFFILIATE_CAPTURE_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getAffiliateAttribution(promise: Promise) {
        try {
            val attribution = LinkzlySDK.getAffiliateAttribution()
            val result = Arguments.createMap()
            result.putString("clickId", attribution.clickId)
            result.putString("programId", attribution.programId)
            result.putString("affiliateId", attribution.affiliateId)
            attribution.timestamp?.let { result.putDouble("timestamp", it.toDouble()) }
                ?: result.putNull("timestamp")
            result.putBoolean("hasAttribution", attribution.hasAttribution)
            result.putString("source", attribution.source.name.lowercase())
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("AFFILIATE_GET_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun getAffiliateClickId(promise: Promise) {
        try {
            val clickId = LinkzlySDK.getAffiliateClickId()
            promise.resolve(clickId)
        } catch (e: Exception) {
            promise.reject("AFFILIATE_CLICK_ID_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun hasAffiliateAttribution(promise: Promise) {
        try {
            val has = LinkzlySDK.hasAffiliateAttribution()
            promise.resolve(has)
        } catch (e: Exception) {
            promise.reject("AFFILIATE_HAS_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun clearAffiliateAttribution(promise: Promise) {
        try {
            LinkzlySDK.clearAffiliateAttribution()
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("AFFILIATE_CLEAR_ERROR", e.message, e)
        }
    }

    // MARK: - Debug APIs (Only available in DEBUG builds)

    @ReactMethod
    fun debugSetBatchingStrategy(strategy: String, promise: Promise) {
        try {
            if (!LinkzlySDKDebug.isAppDebuggable(reactAppContext)) {
                promise.reject("DEBUG_ONLY", "Debug methods are only available in debuggable app builds", null)
                return
            }

            LinkzlySDKDebug.setBatchingStrategy(reactAppContext, strategy)
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("DEBUG_SET_STRATEGY_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun debugSetBatchSize(size: Int, promise: Promise) {
        try {
            if (!LinkzlySDKDebug.isAppDebuggable(reactAppContext)) {
                promise.reject("DEBUG_ONLY", "Debug methods are only available in debuggable app builds", null)
                return
            }

            LinkzlySDKDebug.setBatchSize(reactAppContext, size)
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("DEBUG_SET_BATCH_SIZE_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun debugSetFlushInterval(intervalSeconds: Double, promise: Promise) {
        try {
            if (!LinkzlySDKDebug.isAppDebuggable(reactAppContext)) {
                promise.reject("DEBUG_ONLY", "Debug methods are only available in debuggable app builds", null)
                return
            }

            LinkzlySDKDebug.setFlushInterval(reactAppContext, intervalSeconds.toLong())
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("DEBUG_SET_FLUSH_INTERVAL_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun debugSimulateServerConfig(
        batchSize: Int,
        flushInterval: Double,
        ttl: Double,
        strategy: String?,
        promise: Promise
    ) {
        try {
            if (!LinkzlySDKDebug.isAppDebuggable(reactAppContext)) {
                promise.reject("DEBUG_ONLY", "Debug methods are only available in debuggable app builds", null)
                return
            }

            LinkzlySDKDebug.simulateServerConfig(
                reactAppContext,
                batchSize,
                flushInterval.toLong(),
                ttl.toLong(),
                strategy
            )
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("DEBUG_SIMULATE_CONFIG_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun debugResetConfig(promise: Promise) {
        try {
            if (!LinkzlySDKDebug.isAppDebuggable(reactAppContext)) {
                promise.reject("DEBUG_ONLY", "Debug methods are only available in debuggable app builds", null)
                return
            }

            LinkzlySDKDebug.resetDebugConfig(reactAppContext)
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("DEBUG_RESET_CONFIG_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun debugPrintConfig(promise: Promise) {
        try {
            if (!LinkzlySDKDebug.isAppDebuggable(reactAppContext)) {
                promise.reject("DEBUG_ONLY", "Debug methods are only available in debuggable app builds", null)
                return
            }

            LinkzlySDKDebug.printDebugConfig(reactAppContext)
            val result = Arguments.createMap()
            result.putBoolean("success", true)
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("DEBUG_PRINT_CONFIG_ERROR", e.message, e)
        }
    }

    @ReactMethod
    fun debugGetConfig(promise: Promise) {
        try {
            if (!LinkzlySDKDebug.isAppDebuggable(reactAppContext)) {
                promise.reject("DEBUG_ONLY", "Debug methods are only available in debuggable app builds", null)
                return
            }

            val strategy = LinkzlySDKDebug.getDebugStrategy(reactAppContext)
            val batchSize = LinkzlySDKDebug.getDebugBatchSize(reactAppContext)
            val flushInterval = LinkzlySDKDebug.getDebugFlushInterval(reactAppContext)

            if (strategy == null && batchSize == null && flushInterval == null) {
                promise.resolve(null)
            } else {
                val config = Arguments.createMap()
                strategy?.let { config.putString("strategy", it) }
                batchSize?.let { config.putInt("batchSize", it) }
                flushInterval?.let { config.putDouble("flushInterval", it.toDouble()) }
                promise.resolve(config)
            }
        } catch (e: Exception) {
            promise.reject("DEBUG_GET_CONFIG_ERROR", e.message, e)
        }
    }

    private fun convertMapToWritableMap(map: Map<String, Any>): WritableMap {
        val writableMap = Arguments.createMap()

        for ((key, value) in map) {
            when (value) {
                is String -> writableMap.putString(key, value)
                is Int -> writableMap.putInt(key, value)
                is Double -> writableMap.putDouble(key, value)
                is Boolean -> writableMap.putBoolean(key, value)
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    writableMap.putMap(key, convertMapToWritableMap(value as Map<String, Any>))
                }
                is List<*> -> {
                    writableMap.putArray(key, convertListToWritableArray(value))
                }
                else -> writableMap.putString(key, value.toString())
            }
        }

        return writableMap
    }

    private fun convertListToWritableArray(list: List<*>): WritableArray {
        val writableArray = Arguments.createArray()

        for (item in list) {
            when (item) {
                is String -> writableArray.pushString(item)
                is Int -> writableArray.pushInt(item)
                is Double -> writableArray.pushDouble(item)
                is Boolean -> writableArray.pushBoolean(item)
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    writableArray.pushMap(convertMapToWritableMap(item as Map<String, Any>))
                }
                is List<*> -> writableArray.pushArray(convertListToWritableArray(item))
                else -> writableArray.pushString(item.toString())
            }
        }

        return writableArray
    }

    companion object {
        private var latestInstance: LinkzlyReactNativeModule? = null

        fun getLatestInstance(): LinkzlyReactNativeModule? {
            return latestInstance
        }
    }
}
