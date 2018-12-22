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

import com.mdac.vertx.web.accesslogger.appender.AppenderOptions;

public class ElasticSearchAppenderOptions extends AppenderOptions {

	private PrintStream printStream;
	
	public ElasticSearchAppenderOptions(){
		this.setAppenderImplementationClassName(ElasticSearchAppender.class.getName());
	}
	
	
	/**
	 * dd
	 *
	 * @param printStream
	 * 
	 * @return a reference to this, so the API can be used fluently
	 */
	public ElasticSearchAppenderOptions setPrintStream(final PrintStream printStream) {
		
		if (printStream == null ) {
			throw new IllegalArgumentException("printStream must not be null");
		}
		
		this.printStream = printStream;
		return this;
	}
	

	public PrintStream getPrintStream() {
		
		return printStream;
		
	}
	
}
