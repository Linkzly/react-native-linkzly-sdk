export const __esModule: boolean;
/**
 * Serialize a trackEventBatch list for the native bridge.
 * Each event keeps its other fields; its properties are serialized into
 * `parameters` (read by iOS) and, on Android, also into `customData`, which
 * is the only key the Android SDK's trackEventBatch reads up to now.
 * Never throws.
 */
export function serializeBatchEvents(events: any, platform: any, warn?: (message: any) => void): any[];
/**
 * Serialize one event's properties for the native bridge.
 * Always returns a plain object; never throws.
 */
export function serializeProperties(eventName: any, properties: any, warn?: (message: any) => void): {};
//# sourceMappingURL=serializeProperties.d.ts.map