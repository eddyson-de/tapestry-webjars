package de.eddyson.tapestry.webjars;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.AbstractResource;
import org.slf4j.Logger;
import org.webjars.MultipleMatchesException;
import org.webjars.WebJarAssetLocator;

public class WebjarsResource extends AbstractResource {

  public static final String VERSION_VARIABLE = "$version";

  private static final Pattern versionVariablePattern = Pattern.compile(Pattern.quote(VERSION_VARIABLE));

  // Guarded by lock
  private URL url;

  // Guarded by lock
  private boolean urlResolved;

  private final WebJarAssetLocator webJarAssetLocator;

  private final ClassLoader classLoader;

  private final Logger logger;

  private final Map<String, String> webjars;

  public WebjarsResource(final String path, final WebJarAssetLocator webJarAssetLocator,
      final LoggerSource loggerSource, final ClassLoader classLoader) {
    this(path, webJarAssetLocator, loggerSource.getLogger(WebjarsResource.class), classLoader);

  }

  public WebjarsResource(final String path, final WebJarAssetLocator webJarAssetLocator, final Logger logger,
      final ClassLoader classLoader) {
    super(path);
    this.logger = logger;
    this.webJarAssetLocator = webJarAssetLocator;
    this.webjars = Collections.unmodifiableMap(webJarAssetLocator.getWebJars());
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

  private String resolveVersionInPath(final String path) {
    int indexOfVersionVariable = path.indexOf(VERSION_VARIABLE);

    if (indexOfVersionVariable >= 0) {
      List<String> candidates = new LinkedList<>();
      for (Entry<String, String> e : webjars.entrySet()) {
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

  @Override
  protected Resource newResource(final String path) {
    String p = path;
    if (!p.isEmpty() && p.charAt(0) == '/') {
      p = p.substring(1);
      final String assetPath = p;
      int indexOfColon = p.indexOf(':');
      try {
        if (indexOfColon < 0) {
          logger.warn(
              "Trying to resolve {}. This is inefficient, please include the webjar name in your resource path.",
              assetPath);
          p = resolveVersionInPath(p);
          p = webJarAssetLocator.getFullPath(p);
        } else {
          String webjar = p.substring(0, indexOfColon);
          p = p.substring(indexOfColon + 1);

          String maybeVersion = webjars.get(webjar);
          if (maybeVersion != null) {
            StringBuffer sb = new StringBuffer(p.length());
            Matcher m = versionVariablePattern.matcher(p);
            boolean versionNumberAtStart = false;
            while (m.find()) {
              if (m.start() == 0) {
                versionNumberAtStart = true;
              }
              m.appendReplacement(sb, maybeVersion);
            }
            m.appendTail(sb);
            p = sb.toString();
            if (versionNumberAtStart) {
              p = WebJarAssetLocator.WEBJARS_PATH_PREFIX + "/" + webjar + "/" + p;
            } else {
              logger.warn(
                  "Trying to resolve {}. For optimal performance, the asset location should be absolute, including the version number or the version number placeholder, e.g \"webjars:jquery:$version/dist/jquery.js\".",
                  assetPath);
              p = webJarAssetLocator.getFullPath(webjar, p);
            }

          } else {
            return new WebjarsResource(path, webJarAssetLocator, logger, classLoader);

          }

        }

      } catch (MultipleMatchesException e) {
        throw e;
      } catch (IllegalArgumentException e) {
        return new WebjarsResource(path, webJarAssetLocator, logger, classLoader);
      }
    }
    return new WebjarsResource(p, webJarAssetLocator, logger, classLoader);
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

    if (webJarAssetLocator == null) {
      if (other.webJarAssetLocator != null) {
        return false;
      }
    } else if (!webJarAssetLocator.equals(other.webJarAssetLocator)) {
      return false;
    }
    if (getPath() == null) {
      if (other.getPath() != null) {
        return false;
      }
    } else if (!getPath().equals(other.getPath())) {
      return false;
    }
    return true;
  }

}
