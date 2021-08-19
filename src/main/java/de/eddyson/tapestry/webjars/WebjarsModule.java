package de.eddyson.tapestry.webjars;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetRequestDispatcher;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.webjars.WebJarAssetLocator;

public final class WebjarsModule {

  public static void bind(final ServiceBinder binder) {
    binder.bind(AssetFactory.class, WebjarsAssetFactory.class).withSimpleId().eagerLoad();
    binder.bind(AssetRequestHandler.class, WebjarsAssetRequestHandler.class).withSimpleId().eagerLoad();
  }

  @Contribute(AssetSource.class)
  public static void addWebJarsAssetFactory(final MappedConfiguration<String, AssetFactory> configuration,
      @Local final AssetFactory webJarsAssetFactory) {
    configuration.add("webjars", webJarsAssetFactory);
  }

  @Contribute(Dispatcher.class)
  @AssetRequestDispatcher
  public static void addWebJarsAssetRequestHandler(final MappedConfiguration<String, AssetRequestHandler> configuration,
      @Local final AssetRequestHandler webJarsAssetRequestHandler) {
    configuration.add("webjars", webJarsAssetRequestHandler);
  }

  @EagerLoad
  public static WebJarAssetLocator buildWebJarAssetLocator() {
    return new WebJarAssetLocator();
  }

  public static AssetPathResolver build(final WebJarAssetLocator webJarAssetLocator, final LoggerSource loggerSource) {
    return new AssetPathResolverImpl(webJarAssetLocator, loggerSource);
  }

  private WebjarsModule() {
  }

}
