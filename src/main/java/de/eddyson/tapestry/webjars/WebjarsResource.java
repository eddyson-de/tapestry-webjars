package de.eddyson.tapestry.webjars;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.AbstractResource;
import org.slf4j.Logger;
import org.webjars.MultipleMatchesException;
import org.webjars.WebJarAssetLocator;

public class WebjarsResource extends AbstractResource {

  public static final String VERSION_VARIABLE = "$version";

  // Guarded by lock
  private URL url;

  // Guarded by lock
  private boolean urlResolved;

  private String fullPath;

  private final WebJarAssetLocator webJarAssetLocator;

  private final ClassLoader classLoader;

  private final Logger logger;

  public WebjarsResource(final String path, final WebJarAssetLocator webJarAssetLocator,
      final LoggerSource loggerSource, final ClassLoader classLoader) {
    this(path, webJarAssetLocator, loggerSource.getLogger(WebjarsResource.class), classLoader);

  }

  public WebjarsResource(final String path, final WebJarAssetLocator webJarAssetLocator, final Logger logger,
      final ClassLoader classLoader) {
    super(path);
    this.logger = logger;
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
          String path = getPath();
          int indexOfColon = path.indexOf(':');
          String fullPath;
          if (indexOfColon < 0) {
            path = resolveVersionInPath(path);
            logger.debug("Trying to resolve {}", path);
            fullPath = webJarAssetLocator.getFullPath(path);
          } else {
            String webjar = path.substring(0, indexOfColon);
            path = path.substring(indexOfColon + 1);
            path = resolveVersionInPath(webjar, path);
            logger.debug("Trying to resolve {} inside {} webjar", path, webjar);
            fullPath = webJarAssetLocator.getFullPath(webjar, resolveVersionInPath(webjar, path));
          }
          url = classLoader.getResource(fullPath);
          this.fullPath = fullPath.substring(WebJarAssetLocator.WEBJARS_PATH_PREFIX.length() + 1);

        } catch (MultipleMatchesException e) {
          throw e;
        } catch (IllegalArgumentException e) {
          url = null;
        }
        urlResolved = true;
      }
    } finally {
      downgradeWriteLockToReadLock();
    }
  }

  private String resolveVersionInPath(final String path) {
    int indexOfVersionVariable = path.indexOf(VERSION_VARIABLE);
    if (indexOfVersionVariable >= 0) {
      List<String> candidates = new LinkedList<>();
      for (Entry<String, String> e : webJarAssetLocator.getWebJars().entrySet()) {
        String webjar = e.getKey();
        String version = e.getValue();
        StringBuilder sb = new StringBuilder(path);
        sb.replace(indexOfVersionVariable, indexOfVersionVariable + VERSION_VARIABLE.length(), version);
        logger.debug("Trying {} in {}", sb, webjar);
        try {
          String match = webJarAssetLocator.getFullPath(webjar, sb.toString());
          if (match != null) {
            logger.debug("Found candidate {} for {}", match, path);
            candidates.add(match);
          }
        } catch (MultipleMatchesException ex) {
          candidates.addAll(ex.getMatches());

        } catch (IllegalArgumentException ex) {
          continue;
        }
      }
      if (candidates.size() == 1) {
        return candidates.get(0);
      } else {
        throw new MultipleMatchesException("Found multiple candidates for " + path
            + ", please specify a more specific path. e.g. by including the webjar in the asset path", candidates);
      }

    }
    return path;

  }

  private String resolveVersionInPath(final String webjar, final String path) {
    int indexOfVersionVariable = path.indexOf(VERSION_VARIABLE);
    if (indexOfVersionVariable >= 0) {
      String maybeVersion = webJarAssetLocator.getWebJars().get(webjar);
      logger.debug("Resolved {} for {} to {}", VERSION_VARIABLE, webjar, maybeVersion);
      if (maybeVersion != null) {
        StringBuilder sb = new StringBuilder(path);
        sb.replace(indexOfVersionVariable, indexOfVersionVariable + VERSION_VARIABLE.length(), maybeVersion);
        return sb.toString();
      }

    }
    return path;
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
    return new WebjarsResource(path, webJarAssetLocator, logger, classLoader);
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
