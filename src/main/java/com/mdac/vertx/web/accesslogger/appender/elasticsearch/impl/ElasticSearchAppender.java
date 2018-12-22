/*
 * Copyright (c) 2018 Roman Pierson
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

import java.io.PrintStream;
import java.util.Collection;

import com.mdac.vertx.web.accesslogger.appender.Appender;
import com.mdac.vertx.web.accesslogger.configuration.element.AccessLogElement;

import io.vertx.core.json.JsonObject;

public class ElasticSearchAppender implements Appender {

	private final PrintStream printStream;
	private final Collection<AccessLogElement> accessLogElements;
	
	public ElasticSearchAppender(final ElasticSearchAppenderOptions appenderOptions, final Collection<AccessLogElement> accessLogElements){
		
		// TODO validate
		
		this.printStream = appenderOptions.getPrintStream();
		this.accessLogElements = accessLogElements;
		
	}
	
	
	@Override
	public void push(Collection<JsonObject> accessEvents) {
		
		for(JsonObject accessEvent : accessEvents){
			
			JsonObject indexEntry = new JsonObject();
			
			for(final AccessLogElement alElement : accessLogElements){
				String value = alElement.getFormattedValue(accessEvent);
				indexEntry.put(alElement.getClass().toString(), value);
			}
			
			this.printStream.println(indexEntry.encodePrettily());
			
		}
		
	}
	
	
}
