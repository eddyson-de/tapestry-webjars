# tapestry-webjars [![Build Status](https://travis-ci.org/eddyson-de/tapestry-webjars.svg?branch=master)](https://travis-ci.org/eddyson-de/tapestry-webjars)
Use WebJars (http://www.webjars.org/) together with Tapestry (http://tapestry.apache.org/).

From http://www.webjars.org/:

"WebJars are client-side web libraries (e.g. jQuery & Bootstrap) packaged into JAR (Java Archive) files."

This library provides basic integration for using resources from WebJars as Tapestry assets.

## Usage

A basic usage example to override Tapestry's shipped jQuery library with a newer version.

### `build.gradle`:
```groovy
respositories {
  jcenter()
}

dependencies {
  runtime 'de.eddyson:tapestry-webjars:0.5.0'
  runtime 'org.webjars:jquery:2.1.3'
}

```

### Application Module:
```java
@Contribute(JavaScriptStack.class)
@Core
public static void overrideJQueryWithNewerVersion(final OrderedConfiguration<StackExtension> configuration) {
 configuration.override("jquery-library", StackExtension.library("webjars:jquery.js"));
}
```
