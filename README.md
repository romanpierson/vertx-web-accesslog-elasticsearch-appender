[![Build Status](https://travis-ci.org/romanpierson/vertx-web-accesslog-elasticsearch-appender.svg?branch=master)](https://travis-ci.org/romanpierson/vertx-web-accesslog-elasticsearch-appender) ![Awesome](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg)

# vertx-web-accesslog-elasticsearch-appender

An appender implementation to be used with [vertx-web-accesslog] (https://github.com/romanpierson/vertx-web-accesslog).

Writes its data to an elasticsearch instance.


## Technical Usage

The artefact is published on bintray / jcenter (https://bintray.com/romanpierson/maven/com.mdac.vertx-web-accesslog-elasticsearch-appender)

Just add it as a dependency to your project (gradle example)

```xml
dependencies {
	compile 'vertx-web-accesslog-elasticsearch-appender:1.2.0'
}
```

## Usage

### Configure route

Just put an instance of AccessLogHandler as first route handler

```java
Router router = Router.router(vertx);

router
	.route()
		.handler(AccessLoggerHandler.create(new AccessLoggerOptions().setPattern("%{yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}t %m %s %D cs-uri"), 
			Arrays.asList(
				new ElasticSearchAppenderOptions()
					.setHost("localhost")
				    .setPort(9200)     			
				    .setIndexScheduleInterval(5000L)
				    .setFieldNames(Arrays.asList("timestamp", "method", "status", "duration", "uri"))
				    .setIndexMode(IndexMode.DATE_PATTERN)
				    .setIndexNameOrPattern("accesslog-yyyy-MM-dd")
				)
			)
		)
);
```

### Conventions

Only fix convention for this to use is that the first pattern element needs to be defined as `%{yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}`t.

### Field Names Definition

As for each field to be indexed a specific name needs to be used this has to be explicitly set by `ElasticSearchAppenderOptions.setFieldNames`

### Index creation
 
The solution supports two ways of index creation. For `IndexMode.STATIC_NAME` you must specify a plain index name. For `IndexMode.DATE_PATTERN` you must specify a pattern that can contain placeholders for year, month and day. The indexer will ensure that each access log entry based on its timestamp is indexed to the correct index.

### Using SSL

Currently SSL support is very basic. To activate it just add this to `ElasticSearchAppenderOptions`

```java
	.setSSL(true, "ES_USER", "ES_PW")
```

In order to simplify things for now its only possible to set SSL with Basic Authentication and optionally trust all certificates. ES also supports Authentication via OAuth tokens but this is not supported for now.

## Setup ES Cluster

In order to simplify testing this project contains two docker-compose setups including each a simple ES cluster and Kibana instance (one version with SSL).

### Define global index template

In theory indexing ES is able to derive its own field mapping but its a better option to create an index template as with this you get the correct datatypes.

```json
PUT _template/template_accesslog
{
  "index_patterns": ["accesslog", "accesslog*"],
  "settings": {
    "number_of_shards": 1
  },
  "mappings": {
    "_doc": {
      "_source": {
        "enabled": true
      },
      "properties": {
        "method": {
          "type": "keyword"
        },
        "status": {
          "type": "integer"
        },
        "duration": {
          "type": "integer"
        },
        "timestamp": {
          "type": "date"
        }
      }
    }
  }
}
```

## Compatibility

Version | Accesslog version | ES version
----|------ | ----
1.2.0 | 1.2.0 | 6.5.4 (Check earlier 6x versions)

## Known Issues and to be fixed

* Failure handling is implemented BUT there is no mechanism built yet to retry / temporary store the failed entries 


## Changelog

### 1.2.0

* Initial version


