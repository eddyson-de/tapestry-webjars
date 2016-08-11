package de.eddyson.tapestry.webjars;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.internal.util.VirtualResource;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.javascript.AMDWrapper;
import org.apache.tapestry5.services.javascript.JavaScriptModuleConfiguration;
import org.apache.tapestry5.services.javascript.ModuleManager;

/**
 * Used to wrap CommonJS libraries as AMD modules. The underlying resource is
 * transformed before it is sent to the client.
 *
 * @see JavaScriptModuleConfiguration
 * @see ModuleManager
 * @see AMDWrapper
 * @see <a href="http://requirejs.org/docs/commonjs.html#manualconversion">http:
 *      //requirejs.org/docs/commonjs.html#manualconversion</a>
 */
public class CommonJSAMDWrapper {

  private static final Pattern STARTOFINPUT = Pattern.compile("\\A");

  /**
   * The underlying resource, usually a JavaScript library
   */
  private final Resource resource;

  public CommonJSAMDWrapper(final Resource resource) {
    this.resource = resource;
  }

  /**
   * Return this wrapper instance as a {@link JavaScriptModuleConfiguration}, so
   * it can be contributed to the {@link ModuleManager}'s configuration. The
   * resulting {@link JavaScriptModuleConfiguration} should not be changed.
   *
   * @return a {@link JavaScriptModuleConfiguration} for this AMD wrapper
   */
  public JavaScriptModuleConfiguration asJavaScriptModuleConfiguration() {
    return new JavaScriptModuleConfiguration(transformResource());
  }

  private Resource transformResource() {
    return new AMDModuleWrapperResource(resource);
  }

  /**
   * A virtual resource that wraps a plain JavaScript library as an AMD module.
   *
   */
  private final static class AMDModuleWrapperResource extends VirtualResource {
    private final Resource resource;

    public AMDModuleWrapperResource(final Resource resource) {
      this.resource = resource;

    }

    @Override
    public InputStream openStream() throws IOException {

      Vector<InputStream> v = new Vector<InputStream>(3);
      // https://github.com/jrburke/requirejs/issues/1476
      try (InputStream is = resource.openStream(); Scanner scanner = new Scanner(is, "utf-8")) {
        scanner.useDelimiter(STARTOFINPUT);
        String content = scanner.next();
        Pattern pattern = Pattern.compile("(?<=require\\()(['\"])(.*?)(?:\\.js)?\\1(?=\\))");
        Matcher matcher = pattern.matcher(content);
        JSONArray dependencies = new JSONArray("require", "exports", "module");
        StringBuffer sb = new StringBuffer(content.length());
        while (matcher.find()) {
          String module = matcher.group(2);
          // rewrite require('module.js') to require('module')
          matcher.appendReplacement(sb, "$1$2$1");
          dependencies.put(module);
        }
        matcher.appendTail(sb);

        content = sb.toString();
        InputStream leaderStream = toInputStream(
            "define(" + dependencies.toCompactString() + ", function(require, exports, module) {\n");

        InputStream trailerStream = toInputStream("\n});");
        v.add(leaderStream);
        v.add(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        v.add(trailerStream);

      }

      return new SequenceInputStream(v.elements());
    }

    @Override
    public String getFile() {
      return "generated-module-for-" + resource.getFile();
    }

    @Override
    public URL toURL() {
      return null;
    }

    @Override
    public String toString() {
      return "AMD module wrapper for " + resource.toString();
    }

  }

}
