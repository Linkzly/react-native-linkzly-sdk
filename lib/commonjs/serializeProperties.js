"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.serializeBatchEvents = serializeBatchEvents;
exports.serializeProperties = serializeProperties;
// Turns event properties into values the native bridge can carry, per the
// Linkzly wire contract (spec section 6):
// - text, finite numbers and booleans pass through unchanged;
// - a Date becomes ISO 8601 UTC text with 3 ms digits and "Z";
// - top-level null / undefined: key left out;
// - top-level NaN, Infinity, invalid Date, BigInt, function, symbol or any
//   other unsupported object: property dropped with one warning, event still sent;
// - inside a list or object, a value that can't be sent becomes null
//   (nested null stays null, nested undefined becomes null);
// - reserved keys log a warning but are still sent.
// Nothing here throws.

// Keys Linkzly writes into every SDK event, plus the reserved "__" prefix.
const RESERVED_KEYS = new Set(['parser_version', 'provider_name', 'provider_slug', 'referrer_domain', 'attribution_signal', 'attribution_confidence', 'traffic_channel', 'bot_score', 'bot_name', 'bot_reason', 'device_data']);
const DROP = Symbol('drop');
const defaultWarn = message => {
  console.warn(`[LinkzlySDK] ${message}`);
};
function safeWarn(warn, message) {
  try {
    warn(message);
  } catch {
    // A broken logger must not stop the event.
  }
}
function isPlainObject(value) {
  const proto = Object.getPrototypeOf(value);
  return proto === Object.prototype || proto === null;
}
function describeValue(value) {
  if (typeof value === 'number') return String(value);
  if (value instanceof Date) return 'an invalid Date';
  if (typeof value === 'bigint') return 'a BigInt';
  if (typeof value === 'object' && value !== null) {
    const name = value.constructor?.name;
    return name ? `an unsupported object (${name})` : 'an unsupported object';
  }
  return `a ${typeof value}`;
}

// Returns the sendable form of a value, or DROP if it can't be sent.
// `ancestors` holds the lists and objects above this value, to stop cycles.
function convert(value, ancestors) {
  switch (typeof value) {
    case 'string':
    case 'boolean':
      return value;
    case 'number':
      return Number.isFinite(value) ? value : DROP;
    case 'object':
      break;
    default:
      // undefined, bigint, function, symbol
      return DROP;
  }
  if (value === null) return null;
  if (value instanceof Date) {
    const time = value.getTime();
    return Number.isFinite(time) ? value.toISOString() : DROP;
  }
  if (ancestors.has(value)) return DROP;
  if (Array.isArray(value)) {
    ancestors.add(value);
    const out = [];
    for (let i = 0; i < value.length; i++) {
      let item;
      try {
        item = convert(value[i], ancestors);
      } catch {
        item = DROP;
      }
      out.push(item === DROP ? null : item);
    }
    ancestors.delete(value);
    return out;
  }
  if (!isPlainObject(value)) return DROP;
  ancestors.add(value);
  const out = {};
  for (const key of Object.keys(value)) {
    let item;
    try {
      item = convert(value[key], ancestors);
    } catch {
      item = DROP;
    }
    out[key] = item === DROP ? null : item;
  }
  ancestors.delete(value);
  return out;
}

/**
 * Serialize one event's properties for the native bridge.
 * Always returns a plain object; never throws.
 */
function serializeProperties(eventName, properties, warn = defaultWarn) {
  const out = {};
  try {
    if (properties === null || properties === undefined) return out;
    // Any non-array object works as the property bag (its own keys are read,
    // as the bridge did before); only its values must be sendable.
    if (typeof properties !== 'object' || Array.isArray(properties)) {
      safeWarn(warn, `Event "${eventName}": properties must be an object; sending none.`);
      return out;
    }
    for (const key of Object.keys(properties)) {
      let value;
      let converted;
      try {
        value = properties[key];
        if (value === null || value === undefined) continue;
        converted = convert(value, new Set([properties]));
      } catch {
        converted = DROP;
      }
      if (converted === DROP) {
        safeWarn(warn, `Event "${eventName}": property "${key}" is ${describeValue(value)} and can't be sent; it was left out.`);
        continue;
      }
      if (key.startsWith('__') || RESERVED_KEYS.has(key)) {
        safeWarn(warn, `Event "${eventName}": property "${key}" uses a name reserved by Linkzly; it is still sent, but Linkzly's own value may replace it.`);
      }
      out[key] = converted;
    }
  } catch {
    // Keep whatever was built so far.
  }
  return out;
}

/**
 * Serialize a trackEventBatch list for the native bridge.
 * Each event keeps its other fields; its properties are serialized into
 * `parameters` (read by iOS) and, on Android, also into `customData`, which
 * is the only key the Android SDK's trackEventBatch reads up to now.
 * Never throws.
 */
function serializeBatchEvents(events, platform, warn = defaultWarn) {
  const out = [];
  if (!Array.isArray(events)) return out;
  for (const event of events) {
    try {
      if (event === null || typeof event !== 'object' || Array.isArray(event)) {
        continue;
      }
      const source = event;
      const eventName = typeof source.eventName === 'string' ? source.eventName : '';
      const raw = source.parameters !== undefined && source.parameters !== null ? source.parameters : source.customData;
      const properties = serializeProperties(eventName, raw, warn);
      const item = {};
      for (const key of Object.keys(source)) {
        const value = source[key];
        // Only plain fields pass through as they are; properties are rebuilt below.
        if (key !== 'parameters' && key !== 'customData' && (typeof value === 'string' || typeof value === 'boolean' || typeof value === 'number' && Number.isFinite(value))) {
          item[key] = value;
        }
      }
      item.parameters = properties;
      if (platform === 'android') {
        item.customData = properties;
      }
      out.push(item);
    } catch {
      // Skip an event that can't be read at all.
    }
  }
  return out;
}
//# sourceMappingURL=serializeProperties.js.map