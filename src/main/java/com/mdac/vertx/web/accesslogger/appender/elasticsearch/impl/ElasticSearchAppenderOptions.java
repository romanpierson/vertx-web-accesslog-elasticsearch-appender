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

import com.mdac.vertx.web.accesslogger.appender.AppenderOptions;

public class ElasticSearchAppenderOptions extends AppenderOptions {

	private String host;
	private int port;
	private IndexMode indexMode;
	private String indexNameOrPattern;
	private String type = "_doc";
	private long indexScheduleInterval = 5000L;
	
	private boolean isSSL = false;
	private boolean sslTrustAll = false;
	private String basicAuthenticationUser;
	private String basicAuthenticationPassword;
	
	private Collection<String> fieldNames;
	
	public enum IndexMode{
		
		STATIC_NAME,
		DATE_PATTERN
		
	}

	public ElasticSearchAppenderOptions(){
		
		this.setAppenderImplementationClassName(ElasticSearchAppender.class.getName());
		
	}
	
	public ElasticSearchAppenderOptions setHost(final String host) {
		
		this.host = host;
		
		return this;
		
	}
	
	public ElasticSearchAppenderOptions setPort(final int port) {
		
		this.port = port;
		
		return this;
		
	}
	
	public ElasticSearchAppenderOptions setSSL(final boolean sslTrustAll, final String basicAuthenticationUser, final String basicAuthenticationPassword) {
		
		this.isSSL = true;
		this.sslTrustAll = sslTrustAll;
		this.basicAuthenticationUser = basicAuthenticationUser;
		this.basicAuthenticationPassword = basicAuthenticationPassword;
		
		return this;
		
	}
	
	public ElasticSearchAppenderOptions setIndexNameOrPattern(final String indexNameOrPattern) {
		
		this.indexNameOrPattern = indexNameOrPattern;
		
		return this;
		
	}
	
	public ElasticSearchAppenderOptions setType(final String type) {
		
		this.type = type;
		
		return this;
		
	}
	
	public ElasticSearchAppenderOptions setIndexMode(final IndexMode indexMode) {
		
		this.indexMode = indexMode;
		
		return this;
	}
	
	public ElasticSearchAppenderOptions setFieldNames(final Collection<String> fieldNames) {
		
		this.fieldNames = fieldNames;
		
		return this;
	}
	
	public ElasticSearchAppenderOptions setIndexScheduleInterval(final long indexScheduleInterval) {
		
		this.indexScheduleInterval = indexScheduleInterval;
		
		return this;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public IndexMode getIndexMode() {
		return indexMode;
	}

	public String getIndexNameOrPattern() {
		return indexNameOrPattern;
	}

	public String getType() {
		return type;
	}

	public Collection<String> getFieldNames() {
		return fieldNames;
	}

	public long getIndexScheduleInterval() {
		return indexScheduleInterval;
	}

	public boolean isSSL() {
		return isSSL;
	}

	public boolean isSslTrustAll() {
		return sslTrustAll;
	}

	public String getBasicAuthenticationUser() {
		return basicAuthenticationUser;
	}

	public String getBasicAuthenticationPassword() {
		return basicAuthenticationPassword;
	}
	
	
	
	
	
	
}
