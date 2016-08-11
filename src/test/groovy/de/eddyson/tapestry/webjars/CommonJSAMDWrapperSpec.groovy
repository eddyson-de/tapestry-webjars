package de.eddyson.tapestry.webjars

import java.nio.charset.StandardCharsets

import org.apache.tapestry5.internal.util.VirtualResource
import org.apache.tapestry5.ioc.Resource

import spock.lang.Specification

class CommonJSAMDWrapperSpec extends Specification {

  def "Transform CommonJS module"(){
    setup:

    // this comes from deep-equal/lib/key.js
    def moduleContent = '''exports = module.exports = typeof Object.keys === 'function'
  ? Object.keys : shim;

exports.shim = shim;
function shim (obj) {
  var keys = [];
  for (var key in obj) keys.push(key);
  return keys;
}
'''
    Resource resource = new VirtualResource(){
          InputStream openStream() throws IOException {
            return new ByteArrayInputStream(moduleContent.getBytes(StandardCharsets.UTF_8))

          };
        }
    when:
    def wrapper = new CommonJSAMDWrapper(resource)
    def transformedResource = wrapper.asJavaScriptModuleConfiguration().resource
    def transformedContent = transformedResource.openStream().text
    then:
    transformedContent == '''define(["require","exports","module"], function(require, exports, module) {
exports = module.exports = typeof Object.keys === 'function'
  ? Object.keys : shim;

exports.shim = shim;
function shim (obj) {
  var keys = [];
  for (var key in obj) keys.push(key);
  return keys;
}

});'''

  }
}
