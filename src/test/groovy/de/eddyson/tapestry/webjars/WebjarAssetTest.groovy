package de.eddyson.tapestry.webjars

import org.apache.tapestry5.commons.MappedConfiguration
import org.apache.tapestry5.http.services.ApplicationGlobals
import org.apache.tapestry5.http.services.Context
import org.apache.tapestry5.http.services.Request
import org.apache.tapestry5.http.services.RequestGlobals
import org.apache.tapestry5.ioc.annotations.ImportModule
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.modules.TapestryModule
import org.apache.tapestry5.services.AssetSource
import org.webjars.MultipleMatchesException
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

@ImportModule([WebjarsModule, TapestryModule, TestModule])
class WebjarAssetTest extends Specification {

  @Inject
  private ApplicationGlobals applicationGlobals

  @Inject
  private RequestGlobals requestGlobals


  @Inject
  private AssetSource assetSource

  def setup(){
    Context context = Mock()
    applicationGlobals.storeContext(context)
    Request request = Stub {
    }
    HttpServletRequest servletRequest = Stub {
    }
    requestGlobals.storeRequestResponse(request, null)
    requestGlobals.storeServletRequestResponse(servletRequest, null)
  }

  def "Create asset with webjars prefix"(){
    when:
    def cmAsset = assetSource.getUnlocalizedAsset("webjars:codemirror.js")
    then:
    cmAsset != null
    when:
    def content = cmAsset.getResource().openStream().text
    then:
    content.contains "// This is CodeMirror (http://codemirror.net)"
    cmAsset.toClientURL() ==~ /\/assets\/webjars\/[0-9a-f]*\/codemirror\/[0-9.]*\/lib\/codemirror.js/
  }

  def "Create asset for resource that does not exist"(){
    when:
    def cmAsset = assetSource.getUnlocalizedAsset("webjars:doesnotexist.js")
    then:
    Exception e = thrown()
    e.message == '''Unable to locate asset 'webjars:doesnotexist.js' (the file does not exist).'''
  }

  def "Create asset for resource in a webjar that does not exist"(){
    when:
    def cmAsset = assetSource.getUnlocalizedAsset("webjars:doesnotexist:index.js")
    then:
    Exception e = thrown()
    e.message ==  '''Unable to locate asset 'webjars:doesnotexist:index.js' (the file does not exist).'''
  }

  def "Creating an asset with multiple matches throws an exception"(){
    when:
    def cmAsset = assetSource.getUnlocalizedAsset("webjars:index.js")
    then:
    MultipleMatchesException e = thrown()
    e.matches == [
      'META-INF/resources/webjars/babel-core/6.14.0/index.js',
      'META-INF/resources/webjars/babel-core/6.14.0/lib/transformation/file/options/index.js',
      'META-INF/resources/webjars/babel-core/6.14.0/lib/transformation/file/index.js'
    ]
  }

  def "Create webjars asset with webjar specified"(){
    when:
    def cmAsset = assetSource.getUnlocalizedAsset("webjars:codemirror:codemirror.js")
    then:
    cmAsset != null
    when:
    def content = cmAsset.getResource().openStream().text
    then:
    content.contains "// This is CodeMirror (http://codemirror.net)"
  }

  def "Create webjars asset with version placeholder and explicit webjar"(){
    when:
    def asset = assetSource.getUnlocalizedAsset('webjars:babel-core:$version/index.js')
    then:
    asset != null
    asset.getResource().toURL().toString().contains('6.14.0/index.js')
  }

  def "Create webjars asset with version placeholder"(){
    when:
    def asset = assetSource.getUnlocalizedAsset('webjars:$version/index.js')
    then:
    asset != null
    asset.getResource().toURL().toString().contains('6.14.0/index.js')
  }

  def "Trying to create webjar for an asset that occurs multiple times throws an exception"(){
    when:
    def asset = assetSource.getUnlocalizedAsset('webjars:$version/README.md')
    then:
    MultipleMatchesException e = thrown()
    e.matches == [
      'META-INF/resources/webjars/babel-core/6.14.0/README.md',
      'META-INF/resources/webjars/codemirror/5.13.2/README.md'
    ]
  }


  def "Test hashCode and equals for the same path"(){
    when:
    def cmResource1 = assetSource.getUnlocalizedAsset("webjars:codemirror.js").getResource()
    def cmResource2 = assetSource.getUnlocalizedAsset("webjars:codemirror.js").getResource()
    then:
    cmResource1.hashCode() == cmResource2.hashCode()
    cmResource1 == cmResource2
  }

  def "Test hashCode and equals for the same full path"(){
    when:
    def cmResource1 = assetSource.getUnlocalizedAsset("webjars:codemirror.js").getResource()
    def cmResource2 = assetSource.getUnlocalizedAsset("webjars:lib/codemirror.js").getResource()
    then:
    cmResource1.hashCode() == cmResource2.hashCode()
    cmResource1 == cmResource2
  }

  def "Load asset from a Thread with a different class loader"(){
    when:
    def cmAsset = null

    Thread.start {
      Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], null as ClassLoader))
      cmAsset =  assetSource.getUnlocalizedAsset("webjars:codemirror.js")
    }.join()
    then:
    cmAsset != null
    when:
    def content = cmAsset.getResource().openStream().text
    then:
    content.contains "// This is CodeMirror (http://codemirror.net)"
  }

  def "References to assets with relative locations are resolved properly"(){
    when:
    def resource = assetSource.getUnlocalizedAsset('webjars:options/index.js').resource
    then:
    resource != null
    when:
    def relative = resource.forFile('../index.js')
    then:
    relative != null
    relative.toURL().toString().contains('6.14.0/lib/transformation/file/index.js')
    when:
    relative = resource.forFile('../../../../index.js')
    then:
    relative != null
    relative.toURL().toString().contains('6.14.0/index.js')
  }

  static class TestModule {
    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration){
      configuration.add("tapestry.app-package", "test")
    }
  }
}
