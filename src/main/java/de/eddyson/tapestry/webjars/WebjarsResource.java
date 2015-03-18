package de.eddyson.tapestry.webjars;

import java.net.URL;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.AbstractResource;
import org.webjars.WebJarAssetLocator;

public class WebjarsResource extends AbstractResource {

  // Guarded by lock
  private URL url;

  // Guarded by lock
  private boolean urlResolved;

  private String fullPath;

  private final WebJarAssetLocator webJarAssetLocator;

  private final ClassLoader classLoader;

  public WebjarsResource(final String path, final WebJarAssetLocator webJarAssetLocator, final ClassLoader classLoader) {
    super(path);
    this.webJarAssetLocator = webJarAssetLocator;
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
        try {
          String fullPath = webJarAssetLocator.getFullPath(getPath());
          url = classLoader.getResource(fullPath);
          this.fullPath = fullPath.substring(WebJarAssetLocator.WEBJARS_PATH_PREFIX.length() + 1);
        } catch (IllegalArgumentException e) {
          url = null;
        }
        urlResolved = true;
      }
    } finally {
      downgradeWriteLockToReadLock();
    }
  }

  public String getFullPath() {

    try {
      acquireReadLock();

      if (!urlResolved) {
        resolveURL();
      }

      return fullPath;
    } finally {
      releaseReadLock();
    }
  }

  @Override
  protected Resource newResource(final String path) {
    return new WebjarsResource(path, webJarAssetLocator, classLoader);
  }

  @Override
  public String toString() {
    return "webjars:" + getPath();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((webJarAssetLocator == null) ? 0 : webJarAssetLocator.hashCode());
    result = prime * result + ((getFullPath() == null) ? 0 : getFullPath().hashCode());
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

    if (webJarAssetLocator == null) {
      if (other.webJarAssetLocator != null) {
        return false;
      }
    } else if (!webJarAssetLocator.equals(other.webJarAssetLocator)) {
      return false;
    }
    if (getFullPath() == null) {
      if (other.getFullPath() != null) {
        return false;
      }
    } else if (!getFullPath().equals(other.getFullPath())) {
      return false;
    }
    return true;
  }

}
