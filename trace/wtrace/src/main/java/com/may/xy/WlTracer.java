package com.may.xy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.util.ThreadLocalScopeManager;

/**
 * <p/>Project Name:trace  
 * <p/>cretate user:mayxys  
 * <p/>Date:2018/6/8 13:13  
 * <p/>Copyright (c) 2018, All Rights Reserved.
 */
public class WlTracer implements Tracer {
	private final Propagator propagator;

	private final ScopeManager scopeManager;

	public WlTracer() {
		this(null, new ThreadLocalScopeManager());
	}

	public WlTracer(Propagator propagator, ScopeManager scopeManager) {
		this.propagator = propagator;
		this.scopeManager = scopeManager;
	}

	@Override
	public ScopeManager scopeManager() {
		return scopeManager;
	}

	public Span activeSpan() {
		return null;
	}

	public SpanBuilder buildSpan(String operationName) {
		return null;
	}

	public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {

	}

	public <C> SpanContext extract(Format<C> format, C carrier) {
		return null;
	}

	public interface Propagator {
		<C> void inject(MockSpan.MockContext ctx, Format<C> format, C carrier);

		<C> MockSpan.MockContext extract(Format<C> format, C carrier);

		Propagator PRINTER = new Propagator() {
			@Override
			public <C> void inject(MockSpan.MockContext ctx, Format<C> format, C carrier) {
				System.out.println("inject(" + ctx + ", " + format + ", " + carrier + ")");
			}

			@Override
			public <C> MockSpan.MockContext extract(Format<C> format, C carrier) {
				System.out.println("extract(" + format + ", " + carrier + ")");
				return null;
			}
		};

		Propagator TEXT_MAP = new Propagator() {
			public static final String SPAN_ID_KEY = "spanid";

			public static final String TRACE_ID_KEY = "traceid";

			public static final String BAGGAGE_KEY_PREFIX = "baggage-";

			@Override
			public <C> void inject(MockSpan.MockContext ctx, Format<C> format, C carrier) {
				if (carrier instanceof TextMap) {
					TextMap textMap = (TextMap) carrier;
					for (Map.Entry<String, String> entry : ctx.baggageItems()) {
						textMap.put(BAGGAGE_KEY_PREFIX + entry.getKey(), entry.getValue());
					}
					textMap.put(SPAN_ID_KEY, String.valueOf(ctx.spanId()));
					textMap.put(TRACE_ID_KEY, String.valueOf(ctx.traceId()));
				}
				else {
					throw new IllegalArgumentException("Unknown carrier");
				}
			}

			@Override
			public <C> MockSpan.MockContext extract(Format<C> format, C carrier) {
				Long traceId = null;
				Long spanId = null;
				Map<String, String> baggage = new HashMap<>();

				if (carrier instanceof TextMap) {
					TextMap textMap = (TextMap) carrier;
					for (Map.Entry<String, String> entry : textMap) {
						if (TRACE_ID_KEY.equals(entry.getKey())) {
							traceId = Long.valueOf(entry.getValue());
						}
						else if (SPAN_ID_KEY.equals(entry.getKey())) {
							spanId = Long.valueOf(entry.getValue());
						}
						else if (entry.getKey().startsWith(BAGGAGE_KEY_PREFIX)) {
							String key = entry.getKey().substring((BAGGAGE_KEY_PREFIX.length()));
							baggage.put(key, entry.getValue());
						}
					}
				}
				else {
					throw new IllegalArgumentException("Unknown carrier");
				}

				if (traceId != null && spanId != null) {
					return new MockSpan.MockContext(traceId, spanId, baggage);
				}

				return null;
			}
		};
	}

	public final class SpanBuilder implements Tracer.SpanBuilder {
		private final String operationName;
		private long startMicros;
		private List<MockSpan.Reference> references = new ArrayList<>();
		private boolean ignoringActiveSpan;
		private Map<String, Object> initialTags = new HashMap<>();

		SpanBuilder(String operationName) {
			this.operationName = operationName;
		}

		@Override
		public SpanBuilder asChildOf(SpanContext parent) {
			return addReference(References.CHILD_OF, parent);
		}

		@Override
		public SpanBuilder asChildOf(Span parent) {
			if (parent == null) {
				return this;
			}
			return addReference(References.CHILD_OF, parent.context());
		}

		@Override
		public SpanBuilder ignoreActiveSpan() {
			ignoringActiveSpan = true;
			return this;
		}

		@Override
		public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
			if (referencedContext != null) {
				this.references.add(new MockSpan.Reference((MockSpan.MockContext) referencedContext, referenceType));
			}
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, String value) {
			this.initialTags.put(key, value);
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, boolean value) {
			this.initialTags.put(key, value);
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, Number value) {
			this.initialTags.put(key, value);
			return this;
		}

		@Override
		public SpanBuilder withStartTimestamp(long microseconds) {
			this.startMicros = microseconds;
			return this;
		}

		@Override
		public Scope startActive(boolean finishOnClose) {
			return MockTracer.this.scopeManager().activate(this.startManual(), finishOnClose);
		}

		@Override
		public MockSpan start() {
			return startManual();
		}

		@Override
		public MockSpan startManual() {
			if (this.startMicros == 0) {
				this.startMicros = MockSpan.nowMicros();
			}
			SpanContext activeSpanContext = activeSpanContext();
			if(references.isEmpty() && !ignoringActiveSpan && activeSpanContext != null) {
				references.add(new MockSpan.Reference((MockSpan.MockContext) activeSpanContext, References.CHILD_OF));
			}
			return new MockSpan(MockTracer.this, operationName, startMicros, initialTags, references);
		}
	}
}
