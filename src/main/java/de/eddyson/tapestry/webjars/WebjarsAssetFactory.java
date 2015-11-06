package de.eddyson.tapestry.webjars;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.internal.services.AbstractAssetFactory;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.webjars.WebJarAssetLocator;

public class WebjarsAssetFactory extends AbstractAssetFactory {

  public WebjarsAssetFactory(final ResponseCompressionAnalyzer compressionAnalyzer,
      final ResourceChangeTracker resourceChangeTracker, final StreamableResourceSource streamableResourceSource,
      final AssetPathConstructor assetPathConstructor, final WebJarAssetLocator webJarAssetLocator,
      final LoggerSource loggerSource) {
    super(compressionAnalyzer, resourceChangeTracker, streamableResourceSource, assetPathConstructor,
        new WebjarsResource("/", webJarAssetLocator, loggerSource, Thread.currentThread().getContextClassLoader()));

  }

  @Override
  public Asset createAsset(final Resource resource) {
    return createAsset(resource, "webjars",
        ((WebjarsResource) resource).getPath().substring(WebJarAssetLocator.WEBJARS_PATH_PREFIX.length() + 1));
  }

}
