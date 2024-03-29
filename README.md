# tapestry-webjars [![status](https://github.com/eddyson-de/tapestry-webjars/actions/workflows/main.yml/badge.svg)](https://github.com/eddyson-de/tapestry-webjars/actions/workflows/main.yml)


[![Join the chat at https://gitter.im/eddyson-de/tapestry-webjars](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/eddyson-de/tapestry-webjars?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Use WebJars (http://www.webjars.org/) together with Tapestry (http://tapestry.apache.org/).

From http://www.webjars.org/:

"WebJars are client-side web libraries (e.g. jQuery & Bootstrap) packaged into JAR (Java Archive) files."

This library provides basic integration for using resources from WebJars as Tapestry assets.

## Usage

A basic usage example to override Tapestry's shipped jQuery library with a newer version.

### `build.gradle`:
```groovy
repositories {
  maven { url "https://jitpack.io" }
}

dependencies {
  implementation 'com.github.eddyson-de:tapestry-webjars:1.2.0'
  runtimeOnly 'org.webjars:jquery:3.6.0'
}

```

### Application Module:
```java
@Contribute(JavaScriptStack.class)
@Core
public static void overrideJQueryWithNewerVersion(
    final OrderedConfiguration<StackExtension> configuration) {
  configuration.override("jquery-library", StackExtension.library("webjars:jquery:$version/dist/jquery.js"));
}
```

### Page class:
Of course, you can also `@Inject` assets from Webjars inside your component classes.
```java
@Inject
@Path("webjars:jquery:$version/dist/jquery.js")
private Asset jQuery;

```

### Page css:
You can also use Webjars asset paths inside your CSS files, be sure to use the 'asset:' prefix inside URLs.
```css
div.colorful {
  background: url('asset:webjars:images/rainbow.png');
}

```



## Path specification

There are several options to specify asset paths. Find some examples below, ordered worst to best.
<dl>
  <dt><code>webjar:file.js</code></dt>
  <dd>Tries to find a file named <code>file.js</code> in any of the Webjars on the classpath, inside any folder. This is likely to lead to errors because of multiple possible matches</dd>

  <dt><code>webjars:subdirectory/file.js</code></dt>
  <dd>Tries to find a file named <code>file.js</code> in a subdirectory named <code>subdirectory</code> inside any of the Webjars. The subdirectory does not have to be in the root path of the Webjar and all Webjars are searched, so there can still be multiple matches, e.g. <code>subdirectory/file.js</code> in webjar1, <code>otherdirectory/subdirectory/file.js</code> in webjar1, and <code>subdirectory/file.js</code> in webjar2.</dd>

  <dt><code>webjars:webjar1:file.js</code></dt>
  <dd>Tries to find a file named <code>file.js</code> in the <code>webjar1</code> Webjar inside any folder. This could still lead to errors if there are multiple files with the same name inside a Webjar inside different directories.</dd>
  
    <dt><code>webjars:webjar1:subdirectory/file.js</code></dt>
  <dd>Tries to find a file named <code>file.js</code> in the <code>webjar1</code> Webjar in a subdirectory named <code>subdirectory</code>. While not very likely, this could still lead to multiple matches, e.g. <code>src/subdirectory/file.js</code>, and <code>test/subdirectory/file.js</code>.</dd>

    <dt><code>webjars:webjar1:$version/subdirectory/file.js</code></dt>
  <dd>Tries to find a file named <code>file.js</code> in the <code>webjar1</code> Webjar in a subdirectory named <code>subdirectory</code> that is located directly under the directory that resembles the version number of the Webjar. This is the most definite path that can be specified.</dd>


</dl>

## Working with CommonJS modules

CommonJS modules cannot be used with RequireJS without further work (see http://requirejs.org/docs/commonjs.html), there is however a helper class that performs the "manual conversion" that is explained in http://requirejs.org/docs/commonjs.html#manualconversion.  
For example, to use [deep-equal](https://github.com/substack/node-deep-equal), you would have to add the following configuration:
```java
  @Contribute(ModuleManager.class)
  public static void addDeepEqualModules(final MappedConfiguration<String, JavaScriptModuleConfiguration> configuration,
      @Path("webjars:deep-equal:index.js") final Resource deepEqual) {
  configuration.add("deep-equal/index",
    new CommonJSAMDWrapper(deepEqual).asJavaScriptModuleConfiguration());
  configuration.add("deep-equal/lib/keys",
    new CommonJSAMDWrapper(deepEqual.forFile("./lib/keys.js")).asJavaScriptModuleConfiguration());
  configuration.add("deep-equal/lib/is_arguments",
    new CommonJSAMDWrapper(deepEqual.forFile("./lib/is_arguments.js")).asJavaScriptModuleConfiguration());
}

```
