#import "LinkzlyReactNative.h"
#if __has_include(<linkzly_react_native_sdk/linkzly_react_native_sdk-Swift.h>)
#import <linkzly_react_native_sdk/linkzly_react_native_sdk-Swift.h>
#elif __has_include("linkzly_react_native_sdk-Swift.h")
#import "linkzly_react_native_sdk-Swift.h"
#endif

@implementation LinkzlyReactNative

RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup {
  return YES;
}

- (NSArray<NSString *> *)supportedEvents {
  return @[ @"LinkzlyDeepLinkReceived", @"LinkzlyUniversalLinkReceived" ];
}

RCT_EXPORT_METHOD(configure : (NSString *)sdkKey environment : (NSInteger)
                      environment resolver : (RCTPromiseResolveBlock)
                          resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift configureWithSdkKey:sdkKey
                                   environment:environment
                                      resolver:resolve
                                      rejecter:reject];
}

RCT_EXPORT_METHOD(handleUniversalLink : (NSString *)url resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift handleUniversalLinkWithUrl:url
                                             resolver:resolve
                                             rejecter:reject];
}

RCT_EXPORT_METHOD(trackInstall : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift trackInstallWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(trackOpen : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift trackOpenWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(trackEvent : (NSString *)eventName parameters : (
    NSDictionary *)parameters resolver : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift trackEventWithEventName:eventName
                                        parameters:parameters
                                          resolver:resolve
                                          rejecter:reject];
}

RCT_EXPORT_METHOD(trackPurchase : (NSDictionary *)parameters resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift trackPurchaseWithParameters:parameters
                                              resolver:resolve
                                              rejecter:reject];
}

RCT_EXPORT_METHOD(trackEventBatch : (NSArray *)events resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift trackEventBatchWithEvents:events
                                            resolver:resolve
                                            rejecter:reject];
}

RCT_EXPORT_METHOD(setUserID : (NSString *)userID resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift setUserIDWithUserID:userID
                                      resolver:resolve
                                      rejecter:reject];
}

RCT_EXPORT_METHOD(getUserID : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift getUserIDWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(setTrackingEnabled : (BOOL)enabled resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift setTrackingEnabledWithEnabled:enabled
                                                resolver:resolve
                                                rejecter:reject];
}

RCT_EXPORT_METHOD(isTrackingEnabled : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift isTrackingEnabledWithResolver:resolve
                                                rejecter:reject];
}

RCT_EXPORT_METHOD(getVisitorID : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift getVisitorIDWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(resetVisitorID : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift resetVisitorIDWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(updateConversionValue : (NSInteger)value resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift updateConversionValueWithValue:value
                                                 resolver:resolve
                                                 rejecter:reject];
}

RCT_EXPORT_METHOD(requestTrackingPermission : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift requestTrackingPermissionWithResolver:resolve
                                                        rejecter:reject];
}

RCT_EXPORT_METHOD(setAdvertisingTrackingEnabled : (BOOL)enabled resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift setAdvertisingTrackingEnabledWithEnabled:enabled
                                                           resolver:resolve
                                                           rejecter:reject];
}

RCT_EXPORT_METHOD(isAdvertisingTrackingEnabled : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift isAdvertisingTrackingEnabledWithResolver:resolve
                                                           rejecter:reject];
}

RCT_EXPORT_METHOD(getIDFA : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift getIDFAWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(getATTStatus : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift getATTStatusWithResolver:resolve rejecter:reject];
}

// MARK: - Flush Events and Pending Count

RCT_EXPORT_METHOD(flushEvents : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift flushEventsWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(getPendingEventCount : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift getPendingEventCountWithResolver:resolve
                                                   rejecter:reject];
}

// MARK: - Gaming Tracking (Additive)

RCT_EXPORT_METHOD(configureGamingTracking : (NSString *)apiKey
                      organizationId : (NSString *)organizationId
                      gameId : (NSString *)gameId
                      environment : (NSInteger)environment
                      options : (NSDictionary *)options
                      resolver : (RCTPromiseResolveBlock)resolve
                      rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift configureGamingTrackingWithApiKey:apiKey
                                               organizationId:organizationId
                                                       gameId:gameId
                                                  environment:environment
                                                      options:options
                                                     resolver:resolve
                                                     rejecter:reject];
}

RCT_EXPORT_METHOD(identifyGamingPlayer : (NSString *)playerId
                      traits : (NSDictionary *)traits
                      resolver : (RCTPromiseResolveBlock)resolve
                      rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift identifyGamingPlayerWithPlayerId:playerId
                                                     traits:traits
                                                   resolver:resolve
                                                   rejecter:reject];
}

RCT_EXPORT_METHOD(trackGamingEvent : (NSString *)eventType
                      data : (NSDictionary *)data
                      immediate : (BOOL)immediate
                      resolver : (RCTPromiseResolveBlock)resolve
                      rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift trackGamingEventWithEventType:eventType
                                                    data:data
                                               immediate:immediate
                                                resolver:resolve
                                                rejecter:reject];
}

RCT_EXPORT_METHOD(flushGamingEvents : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift flushGamingEventsWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(startGamingSession : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift startGamingSessionWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(endGamingSession : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift endGamingSessionWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(setGamingAttribution : (NSString *)clickId
                      deferredDeepLink : (NSString *)deferredDeepLink
                      metadata : (NSDictionary *)metadata
                      resolver : (RCTPromiseResolveBlock)resolve
                      rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift setGamingAttributionWithClickId:clickId
                                          deferredDeepLink:deferredDeepLink
                                                  metadata:metadata
                                                  resolver:resolve
                                                  rejecter:reject];
}

RCT_EXPORT_METHOD(clearGamingAttribution : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift clearGamingAttributionWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(resetGamingTracking : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift resetGamingTrackingWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(getGamingStatus : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift getGamingStatusWithResolver:resolve rejecter:reject];
}

// MARK: - Debug APIs (Only available in DEBUG builds)

RCT_EXPORT_METHOD(debugSetBatchingStrategy : (NSString *)strategy resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift debugSetBatchingStrategyWithStrategy:strategy
                                                       resolver:resolve
                                                       rejecter:reject];
}

RCT_EXPORT_METHOD(debugSetBatchSize : (NSInteger)size resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift debugSetBatchSizeWithSize:size
                                            resolver:resolve
                                            rejecter:reject];
}

RCT_EXPORT_METHOD(debugSetFlushInterval : (double)interval resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift debugSetFlushIntervalWithInterval:interval
                                                    resolver:resolve
                                                    rejecter:reject];
}

RCT_EXPORT_METHOD(
    debugSimulateServerConfig : (NSInteger)batchSize flushInterval : (double)
        flushInterval ttl : (double)ttl strategy : (NSString *)
            strategy resolver : (RCTPromiseResolveBlock)
                resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift debugSimulateServerConfigWithBatchSize:batchSize
                                                    flushInterval:flushInterval
                                                              ttl:ttl
                                                         strategy:strategy
                                                         resolver:resolve
                                                         rejecter:reject];
}

RCT_EXPORT_METHOD(debugResetConfig : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift debugResetConfigWithResolver:resolve
                                               rejecter:reject];
}

RCT_EXPORT_METHOD(debugPrintConfig : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift debugPrintConfigWithResolver:resolve
                                               rejecter:reject];
}

RCT_EXPORT_METHOD(debugGetConfig : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift debugGetConfigWithResolver:resolve rejecter:reject];
}

// MARK: - Push Notification Support

RCT_EXPORT_METHOD(initializePush : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift initializePushWithResolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(disablePush : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift disablePushWithResolver:resolve rejecter:reject];
}

// MARK: - Affiliate Attribution

RCT_EXPORT_METHOD(captureAffiliateAttribution : (NSString *)url resolver : (
    RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift captureAffiliateAttributionWithUrl:url
                                                     resolver:resolve
                                                     rejecter:reject];
}

RCT_EXPORT_METHOD(getAffiliateAttribution : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift getAffiliateAttributionWithResolver:resolve
                                                      rejecter:reject];
}

RCT_EXPORT_METHOD(getAffiliateClickId : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift getAffiliateClickIdWithResolver:resolve
                                                  rejecter:reject];
}

RCT_EXPORT_METHOD(hasAffiliateAttribution : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift hasAffiliateAttributionWithResolver:resolve
                                                      rejecter:reject];
}

RCT_EXPORT_METHOD(clearAffiliateAttribution : (RCTPromiseResolveBlock)
                      resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [LinkzlyReactNativeSwift clearAffiliateAttributionWithResolver:resolve
                                                        rejecter:reject];
}

@end
