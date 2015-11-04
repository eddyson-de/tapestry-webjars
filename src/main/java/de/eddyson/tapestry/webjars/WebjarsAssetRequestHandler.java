package de.eddyson.tapestry.webjars;

import java.io.IOException;

import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.internal.services.assets.ChecksumPath;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.webjars.WebJarAssetLocator;

public class WebjarsAssetRequestHandler implements AssetRequestHandler {

  private final ResourceStreamer streamer;

  private final WebjarsResource rootResource;

  public WebjarsAssetRequestHandler(final ResourceStreamer streamer, final WebJarAssetLocator webJarAssetLocator,
      final LoggerSource loggerSource) {
    this.streamer = streamer;
    this.rootResource = new WebjarsResource("/", webJarAssetLocator, loggerSource,
        Thread.currentThread().getContextClassLoader());
  }

  @Override
  public boolean handleAssetRequest(final Request request, final Response response, final String extraPath)
      throws IOException {
    ChecksumPath path = new ChecksumPath(streamer, null, extraPath);

    return path.stream(rootResource.forFile(path.resourcePath));
  }

}
