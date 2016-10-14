package de.eddyson.tapestry.webjars;

import org.apache.tapestry5.internal.services.ResourceStreamer
import org.apache.tapestry5.ioc.LoggerSource
import org.apache.tapestry5.services.Request
import org.apache.tapestry5.services.Response
import org.slf4j.Logger
import org.webjars.WebJarAssetLocator

import spock.lang.Specification

public class WebjarsAssetRequestHandlerSpec extends Specification {

  def "create asset from request path"(){
    setup:
    ResourceStreamer resourceStreamer = Mock()
    Logger logger = Mock()
    LoggerSource loggerSource = Stub(){ getLogger(_) >> logger }
    WebJarAssetLocator webJarAssetLocator = WebjarsModule.buildWebJarAssetLocator()
    AssetPathResolver assetPathResolver = new AssetPathResolverImpl(webJarAssetLocator, loggerSource)
    WebjarsAssetRequestHandler webjarsAssetRequestHandler = new WebjarsAssetRequestHandler(resourceStreamer, assetPathResolver, loggerSource)
    Request request = Stub {
    }
    Response response = Stub {
    }
    when:
    webjarsAssetRequestHandler.handleAssetRequest(request, response, 'somechecksum/codemirror/5.13.2/lib/codemirror.css')
    then:
    0 * logger.warn(*_)
    1 * resourceStreamer.streamResource(*_)
  }
}
