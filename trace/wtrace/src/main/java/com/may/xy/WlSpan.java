package com.may.xy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opentracing.Span;
import io.opentracing.SpanContext;

/**
 * <p/>Project Name:trace  
 * <p/>cretate user:mayxys  
 * <p/>Date:2018/6/8 13:31  
 * <p/>Copyright (c) 2018, All Rights Reserved.
 * <p/>TODO 说明类用途
 */
public class WlSpan implements Span {
	private String operationName;

	private final long startMicros;

	private boolean finished;

	private WLContext context;

	private final Map<String, Object> tags;

	private final List<LogEntry> logEntries = new ArrayList<>();

	@Override
	public SpanContext context() {
		return this.context;
	}

	@Override
	public Span setTag(String key, String value) {
		return setObjectTag(key, value);
	}

	@Override
	public Span setTag(String key, boolean value) {
		return setObjectTag(key, value);
	}

	@Override
	public Span setTag(String key, Number value) {
		return setObjectTag(key, value);
	}

	@Override
	public Span log(Map<String, ?> fields) {
		return log(nowMicros(), fields);
	}

	@Override
	public Span log(long timestampMicroseconds, Map<String, ?> fields) {
		finishedCheck("Adding logs %s at %d to already finished span", fields, timestampMicroseconds);
		this.logEntries.add(new LogEntry(timestampMicroseconds, fields));
		return this;
	}

	@Override
	public Span log(String event) {
		return log(nowMicros(), event);
	}

	@Override
	public Span log(long timestampMicroseconds, String event) {
		return log(timestampMicroseconds, Collections.singletonMap("event", event));
	}

	@Override
	public synchronized Span setBaggageItem(String key, String value) {
		finishedCheck("Adding baggage {%s:%s} to already finished span", key, value);
		this.context = this.context.withBaggageItem(key, value);
		return this;
	}

	@Override
	public synchronized String getBaggageItem(String key) {
		return this.context.getBaggageItem(key);
	}

	@Override
	public Span setOperationName(String operationName) {
		this.operationName = operationName;
		return this;
	}

	@Override
	public synchronized void finish() {

	}

	@Override
	public void finish(long finishMicros) {
		finishedCheck("Finishing already finished span");
		this.finishMicros = finishMicros;
		this.mockTracer.appendFinishedSpan(this);
		this.finished = true;
	}

	private synchronized WlSpan setObjectTag(String key, Object value) {
		finishedCheck("Adding tag {%s:%s} to already finished span", key, value);
		tags.put(key, value);
		return this;
	}

	private synchronized void finishedCheck(String format, Object... args) {
		if (finished) {
			RuntimeException ex = new IllegalStateException(String.format(format, args));
			errors.add(ex);
			throw ex;
		}
	}

	public static final class LogEntry {
		private final long timestampMicros;

		private final Map<String, ?> fields;

		public LogEntry(long timestampMicros, Map<String, ?> fields) {
			this.timestampMicros = timestampMicros;
			this.fields = fields;
		}

		public long timestampMicros() {
			return timestampMicros;
		}

		public Map<String, ?> fields() {
			return fields;
		}
	}

	static long nowMicros() {
		return System.currentTimeMillis() * 1000;
	}

	public static final class WLContext implements SpanContext {
		private final long traceId;

		private final Map<String, String> baggage;

		private final long spanId;

		/**
		 * A package-protected constructor to create a new WLContext. This should only be called by MockSpan and/or
		 * MockTracer.
		 *
		 * @param baggage the WLContext takes ownership of the baggage parameter
		 *
		 * @see WLContext#withBaggageItem(String, String)
		 */
		public WLContext(long traceId, long spanId, Map<String, String> baggage) {
			this.baggage = baggage;
			this.traceId = traceId;
			this.spanId = spanId;
		}

		public String getBaggageItem(String key) {
			return this.baggage.get(key);
		}

		public long traceId() {
			return traceId;
		}

		public long spanId() {
			return spanId;
		}

		/**
		 * Create and return a new (immutable) WLContext with the added baggage item.
		 */
		public WLContext withBaggageItem(String key, String val) {
			Map<String, String> newBaggage = new HashMap<>(this.baggage);
			newBaggage.put(key, val);
			return new WLContext(this.traceId, this.spanId, newBaggage);
		}

		@Override
		public Iterable<Map.Entry<String, String>> baggageItems() {
			return baggage.entrySet();
		}
	}
}
