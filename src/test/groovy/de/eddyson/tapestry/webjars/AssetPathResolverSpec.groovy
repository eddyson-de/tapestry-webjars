package de.eddyson.tapestry.webjars

import org.apache.tapestry5.ioc.LoggerSource
import org.slf4j.Logger

import spock.lang.Specification

class AssetPathResolverSpec extends Specification {

  def "Create webjars asset with explicit webjar and explicit version"(){
    setup:
    Logger logger = Mock()
    LoggerSource loggerSource = Stub(){ getLogger(_) >> logger }
    AssetPathResolver assetPathResolver = new AssetPathResolverImpl(WebjarsModule.buildWebJarAssetLocator(), loggerSource)

    when:
    def assetPath = assetPathResolver.resolve('babel-core:6.14.0/index.js')
    then:
    new WebjarsResource(assetPath, assetPathResolver, Thread.currentThread().contextClassLoader).exists()
    0 * logger.warn(*_)
  }

  def "Create webjars asset with explicit webjar and explicit but incorrect version"(){
    setup:
    Logger logger = Mock()
    LoggerSource loggerSource = Stub(){ getLogger(_) >> logger }
    AssetPathResolver assetPathResolver = new AssetPathResolverImpl(WebjarsModule.buildWebJarAssetLocator(), loggerSource)

    when:
    def assetPath = assetPathResolver.resolve('babel-core:1.2.3/index.js')
    then:
    !new WebjarsResource(assetPath, assetPathResolver, Thread.currentThread().contextClassLoader).exists()
    0 * logger.warn(*_)
  }
}
