/*
 * Copyright (c) 2016-2019 Roman Pierson
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 
 * which accompanies this distribution.
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package com.mdac.vertx.web.accesslogger.appender.elasticsearch.impl;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mdac.vertx.web.accesslogger.AccessLoggerConstants;
import com.mdac.vertx.web.accesslogger.appender.elasticsearch.impl.ElasticSearchAppenderOptions.IndexMode;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class ElasticSearchAppender extends AbstractVerticle {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass().getName());
	
	private BlockingQueue<JsonArray> queue = new LinkedBlockingQueue<>();

	private ElasticSearchAppenderOptions appenderOptions;
	
	private HttpRequest<Buffer> request;
	private final String newLine = "\n";
	private String cachedStaticIndexPrefix;
	private Map<String, String> cachedDynamicIndexPrefix = new HashMap<>();
	private Collection<String> fieldNames;
	
	public ElasticSearchAppender(final ElasticSearchAppenderOptions appenderOptions){
		
		if(appenderOptions == null){
			throw new IllegalArgumentException("appenderOptions must not be null");
		}
		
		this.appenderOptions = appenderOptions;
		this.fieldNames = appenderOptions.getFieldNames();
	}
	
	@Override
	public void start() throws Exception {

		super.start();

		LOG.info("Starting ElasticSearchAppender Verticle");
		
		vertx.eventBus().<JsonArray> consumer(AccessLoggerConstants.EVENTBUS_APPENDER_EVENT_NAME, event -> {
			
			try {
				this.queue.put(event.body());
			}catch(Exception ex) {
				LOG.error("Error when trying to add event to queue", ex);
			}
			
		});
		
		initializeClient();
		
		vertx.setPeriodic(this.appenderOptions.getIndexScheduleInterval(), handler -> {

			if (!this.queue.isEmpty()) {

				indexCurrentData();
				
			}
		});

	}

	private void indexCurrentData() {
		
		final int currentSize = this.queue.size();
		
		final Collection<JsonArray> drainedValues = new ArrayList<>(currentSize);

		this.queue.drainTo(drainedValues, currentSize);

		request
		  .sendBuffer(Buffer.buffer(getIndexString(drainedValues)), ar -> {
		    if (ar.succeeded()) {
		     
		    	JsonObject response = ar.result().bodyAsJsonObject();
		    	
		    	if(response == null || response.getBoolean("errors")) {
		    		handleError(drainedValues, null);
		    	}
		    	
		    } else {
		    	
		    	handleError(drainedValues, ar.cause());
		    	
		    }
		  });
	}
	
	private void handleError(Collection<JsonArray> events, Throwable throwable ) {
	
		if (throwable != null) {
			LOG.warn("Failed to index [{}] values", events.size(), throwable);
		} else {
			LOG.warn("Failed to index [{}] values");
		}
		
	}
	
	private void initializeClient() {
		
		WebClientOptions options = new WebClientOptions();
		options.setKeepAlive(true);
		
		WebClient client = WebClient.create(vertx, options);
		this.request = client.post(this.appenderOptions.getPort(), this.appenderOptions.getHost(), "/_bulk");
		this.request.putHeader("content-type", "application/json");
		
		LOG.info("Initialized WebClient for [{}:{}]", this.appenderOptions.getHost(), this.appenderOptions.getPort());
		
	}
	
	private String getIndexPrefixString(final String timestamp) {
		
		if(IndexMode.DATE_PATTERN.equals(this.appenderOptions.getIndexMode())) {
		
			final String cacheKey = timestamp.substring(0, 10);
			
			if(!this.cachedDynamicIndexPrefix.containsKey(cacheKey)) {
				
				ZonedDateTime tsDateTime = ZonedDateTime.parse(timestamp);
				// Explicitly not using a DateTimeFormatter as this would require escaping the whole pattern
				String formattedIndexPattern = this.appenderOptions.getIndexNameOrPattern()
													.replaceAll("yyyy", String.format("%04d", tsDateTime.getYear()))
													.replaceAll("MM", String.format("%02d", tsDateTime.getMonthValue()))
													.replaceAll("dd", String.format("%02d", tsDateTime.getDayOfMonth()));
				
				String formattedIndexPrefix = String.format("{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\" } }%s",  formattedIndexPattern, this.appenderOptions.getType(), this.newLine);
				
				this.cachedDynamicIndexPrefix.put(cacheKey, formattedIndexPrefix);
				
			}
			
			return this.cachedDynamicIndexPrefix.get(cacheKey);
			
		} else {
			
			if(this.cachedStaticIndexPrefix == null) {
				
				this.cachedStaticIndexPrefix = String.format("{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\" } }%s",  this.appenderOptions.getIndexNameOrPattern(), this.appenderOptions.getType(), this.newLine);
				
			}
			
			return this.cachedStaticIndexPrefix;
		}
		
	}
	
	private String getIndexString(final Collection<JsonArray> values) {
		
		StringBuilder sb = new StringBuilder();
		
		for(JsonArray value : values) {
			
			String [] parameterValues = getParameterValues(value);
			
			sb.append(getIndexPrefixString(parameterValues[0]));
			
			JsonObject jsonValue = new JsonObject();
			
			int i = 0;
			for(String fieldName : fieldNames) {
				jsonValue.put(fieldName, parameterValues[i]);
				i++;
			}
			
			sb.append(jsonValue.encode()).append(newLine);
			
		}
		
		return sb.toString();
	}
	
	private String[] getParameterValues(final JsonArray values){
		
		final String[] parameterValues = new String[values.size()];

		int i = 0;
		for (final Object xValue : values.getList()) {
			parameterValues[i] = (String) xValue;
			i++;
		}
		
		return parameterValues;
		
	}
	
	@Override
	public void stop() throws Exception {

		LOG.info("Stopping ElasticSearchAppender Verticle");

		if(!this.queue.isEmpty()) {
			
			LOG.info("Starting to drain queue with [{}] items left to ElasticSearch", this.queue.size());
			
			indexCurrentData();
			
			LOG.info("Finished queue draining");
			
		} else {
			
			LOG.info("No items left in queue");
			
		}
		
		super.stop();

	}

	
}
