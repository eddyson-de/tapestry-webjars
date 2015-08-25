package de.eddyson.tapestry.webjars

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.modules.TapestryModule
import org.apache.tapestry5.services.ApplicationGlobals
import org.apache.tapestry5.services.AssetSource
import org.apache.tapestry5.services.Context

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
