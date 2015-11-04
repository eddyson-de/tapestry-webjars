package de.eddyson.tapestry.webjars

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.modules.TapestryModule
import org.apache.tapestry5.services.ApplicationGlobals
import org.apache.tapestry5.services.AssetSource
import org.apache.tapestry5.services.Context
import org.webjars.MultipleMatchesException;

@SubModule([WebjarsModule, TapestryModule])
class WebjarAssetTest extends spock.lang.Specification {

  @Inject
  private ApplicationGlobals applicationGlobals;
  
  @Inject
  private AssetSource assetSource;
  
  def setup(){
    Context context = Mock()
    applicationGlobals.storeContext(context)
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
  }
  
  def "Creating an asset with multiple matches throws an exception"(){
    when:
    def cmAsset = assetSource.getUnlocalizedAsset("webjars:browser.js")
    then:
    MultipleMatchesException e = thrown()
    e.matches == ['META-INF/resources/webjars/babel-core/6.0.20/lib/api/browser.js', 'META-INF/resources/webjars/babel-core/6.0.20/lib/api/register/browser.js', 'META-INF/resources/webjars/babel-core/6.0.20/browser.js']
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
    def asset = assetSource.getUnlocalizedAsset('webjars:babel-core:$version/browser.js')
    then:
    asset != null
    asset.getResource().toURL().toString().contains('6.0.20/browser.js')
  }

  def "Create webjars asset with version placeholder"(){
    when:
    def asset = assetSource.getUnlocalizedAsset('webjars:$version/browser.js')
    then:
    asset != null
    asset.getResource().toURL().toString().contains('6.0.20/browser.js')
  }
  
  def "Trying to create webjar for an asset that occurs multiple times throws an exception"(){
    when:
    def asset = assetSource.getUnlocalizedAsset('webjars:$version/README.md')
    then:
    MultipleMatchesException e = thrown()
    e.matches == ['META-INF/resources/webjars/codemirror/5.7/README.md', 'META-INF/resources/webjars/babel-core/6.0.20/README.md']
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
}
