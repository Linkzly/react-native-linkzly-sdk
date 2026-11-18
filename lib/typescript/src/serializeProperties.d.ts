export type WarnFn = (message: string) => void;
/**
 * Serialize one event's properties for the native bridge.
 * Always returns a plain object; never throws.
 */
export declare function serializeProperties(eventName: string, properties: unknown, warn?: WarnFn): Record<string, unknown>;
/**
 * Serialize a trackEventBatch list for the native bridge.
 * Each event keeps its other fields; its properties are serialized into
 * `parameters` (read by iOS) and, on Android, also into `customData`, which
 * is the only key the Android SDK's trackEventBatch reads up to now.
 * Never throws.
 */
export declare function serializeBatchEvents(events: unknown, platform: string, warn?: WarnFn): Record<string, unknown>[];
//# sourceMappingURL=serializeProperties.d.ts.map