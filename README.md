[![Build Status](https://travis-ci.org/romanpierson/vertx-web-accesslog-elasticsearch-appender.svg?branch=master)](https://travis-ci.org/romanpierson/vertx-web-accesslog-elasticsearch-appender) ![Awesome](https://cdn.rawgit.com/sindresorhus/awesome/d7305f38d29fed78fa85652e3a63e154dd8e8829/media/badge.svg)

# vertx-web-accesslog-elasticsearch-appender

A vertx-web-accesslog-appender instance that writes its data to an elasticsearch instance


## Technical Usage

The artefact is published on bintray / jcenter (https://bintray.com/romanpierson/maven/com.mdac.vertx-web-accesslog)

Just add it as a dependency to your project (maven example)

```xml
<dependency>
  <groupId>com.mdac</groupId>
  <artifactId>vertx-web-accesslog-elasticsearch-appender</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

## Setup ES Cluster

### Define global index template

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

### Compatibility

Version | Accesslog version | ES version
----|------ | ----
1.2.0 | 1.2.0 | TBD

