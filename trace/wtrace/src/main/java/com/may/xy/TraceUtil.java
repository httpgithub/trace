package com.may.xy;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopScopeManager;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

/**
 * <p/>Project Name:trace  
 * <p/>cretate user:mayxys  
 * <p/>Date:2018/6/6 15:10  
 * <p/>Copyright (c) 2018, All Rights Reserved.
 * <p/>TODO 说明类用途
 */
public class TraceUtil {
	static Scope buildScope(String operationName, String sql, String dbType, String dbUser,
			boolean withActiveSpanOnly) {
		if (withActiveSpanOnly && GlobalTracer.get().activeSpan() == null) {
			return NoopScopeManager.NoopScope.INSTANCE;
		}

		Tracer.SpanBuilder spanBuilder = GlobalTracer.get().buildSpan(operationName)
				.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

		Scope scope = spanBuilder.startActive(true);
		decorate(scope.span(), sql, dbType, dbUser);

		return scope;
	}

	private static void decorate(Span span, String sql, String dbType, String dbUser) {
		Tags.COMPONENT.set(span, COMPONENT_NAME);
		Tags.DB_STATEMENT.set(span, sql);
		Tags.DB_TYPE.set(span, dbType);
		if (dbUser != null) {
			Tags.DB_USER.set(span, dbUser);
		}
	}
}
