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

import java.util.Collection;

import com.mdac.vertx.elasticsearch.indexer.ElasticSearchIndexerConstants;
import com.mdac.vertx.elasticsearch.indexer.ElasticSearchIndexerConstants.Message.Structure.Field;
import com.mdac.vertx.elasticsearch.indexer.verticle.ElasticSearchIndexerVerticle;
import com.mdac.vertx.web.accesslogger.appender.Appender;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * An implementation of {@link Appender} that writes to {@link ElasticSearchIndexerVerticle}
 * 
 * @author Roman Pierson
 *
 */
public class ElasticSearchAppender implements Appender {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass().getName());
	
	private static final String CONFIG_KEY_INSTANCE_IDENTIFER = "instanceIdentifier";
	private static final String CONFIG_KEY_TIMESTAMP_POSITION = "timestampPosition";
	private static final String CONFIG_KEY_FIELD_NAMES = "fieldNames";

	private final EventBus vertxEventBus;
	
	private final int timestampPosition;
	private final String instanceIdentifier;
	private final Collection<String> fieldNames;
	
	@SuppressWarnings("unchecked")
	public ElasticSearchAppender(final JsonObject config){
		
		if(config == null){
			throw new IllegalArgumentException("config must not be null");
		} else if (config.getString(CONFIG_KEY_INSTANCE_IDENTIFER, "").trim().length() == 0) {
			throw new IllegalArgumentException(CONFIG_KEY_INSTANCE_IDENTIFER + " must not be empty");
		} else if (config.getJsonArray(CONFIG_KEY_FIELD_NAMES, new JsonArray()).isEmpty()) {
			throw new IllegalArgumentException(CONFIG_KEY_FIELD_NAMES + " must not be empty");
		}
		
		this.vertxEventBus = Vertx.currentContext().owner().eventBus();
		
		this.timestampPosition = config.getInteger(CONFIG_KEY_TIMESTAMP_POSITION, 0);
		this.instanceIdentifier = config.getString(CONFIG_KEY_INSTANCE_IDENTIFER);
		this.fieldNames = config.getJsonArray(CONFIG_KEY_FIELD_NAMES).getList();
		
		LOG.info("Created ElasticSearchAppender with {} [{}], {} [{}], {} {}", CONFIG_KEY_INSTANCE_IDENTIFER, this.instanceIdentifier, CONFIG_KEY_TIMESTAMP_POSITION, this.timestampPosition, CONFIG_KEY_FIELD_NAMES, this.fieldNames);
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
	public void push(final JsonArray accessEvent) {
		
		final long metaTimestamp = this.timestampPosition < 0 ? System.currentTimeMillis() : Long.parseLong(accessEvent.getString(this.timestampPosition));
			
			// Just send the accesslog event to the indexer
			JsonObject jsonMeta = new JsonObject();
			jsonMeta.put(Field.TIMESTAMP.getFieldName(), metaTimestamp);
			jsonMeta.put(Field.INSTANCE_IDENTIFIER.getFieldName(), this.instanceIdentifier);
			
			JsonObject jsonMessage = new JsonObject();
			
			String [] parameterValues = getParameterValues(accessEvent);
			
			int i = 0;
			for(String fieldName : fieldNames) {
				if(this.timestampPosition != i) {
					jsonMessage.put(fieldName, parameterValues[i]);
				}
				i++;
			}
			
			JsonObject json = new JsonObject()
					.put(Field.META.getFieldName(), jsonMeta)
					.put(Field.MESSAGE.getFieldName(), jsonMessage);
			
			this.vertxEventBus.send(ElasticSearchIndexerConstants.EVENTBUS_EVENT_NAME,  json);
			
	}
	
}
