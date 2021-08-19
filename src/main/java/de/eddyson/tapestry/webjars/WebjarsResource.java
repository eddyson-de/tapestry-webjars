package de.eddyson.tapestry.webjars;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.ioc.internal.util.AbstractResource;
import org.webjars.WebJarAssetLocator;

import java.net.URL;

public class WebjarsResource extends AbstractResource {

  public static final String VERSION_VARIABLE = "$version";

  // Guarded by lock
  private URL url;

  // Guarded by lock
  private boolean urlResolved;

  private final ClassLoader classLoader;

  private final AssetPathResolver assetPathResolver;

  public WebjarsResource(final String path, final AssetPathResolver assetPathResolver, final ClassLoader classLoader) {
    super(path);
    this.assetPathResolver = assetPathResolver;
    this.classLoader = classLoader;
  }

  @Override
  public URL toURL() {

    try {
      acquireReadLock();

      if (!urlResolved) {
        resolveURL();
      }

      return url;
    } finally {
      releaseReadLock();
    }
  }

  private void resolveURL() {
    try {
      upgradeReadLockToWriteLock();

      if (!urlResolved) {
        String path = getPath();
        if (path.startsWith(WebJarAssetLocator.WEBJARS_PATH_PREFIX)) {
          url = classLoader.getResource(path);
          validateURL(url);
        }
        urlResolved = true;
      }
    } finally {
      downgradeWriteLockToReadLock();
    }
  }

  @Override
  protected Resource newResource(final String path) {
    if (!path.isEmpty() && path.charAt(0) == '/') {
      String p = assetPathResolver.resolve(path.substring(1));
      return new WebjarsResource(p, assetPathResolver, classLoader);
    }
    return new WebjarsResource(path, assetPathResolver, classLoader);
  }

  @Override
  public String toString() {
    return "webjars:" + getPath();
  }

  public Resource createFromRequestPath(final String path) {
    return new WebjarsResource(WebJarAssetLocator.WEBJARS_PATH_PREFIX + "/" + path, assetPathResolver, classLoader);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((assetPathResolver == null) ? 0 : assetPathResolver.hashCode());
    result = prime * result + ((getPath() == null) ? 0 : getPath().hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    WebjarsResource other = (WebjarsResource) obj;

    if (assetPathResolver == null) {
      if (other.assetPathResolver != null) {
        return false;
      }
    } else if (!assetPathResolver.equals(other.assetPathResolver)) {
      return false;
    }
    if (getPath() == null) {
      return other.getPath() == null;
    } else {
      return getPath().equals(other.getPath());
    }
  }
}
