package de.eddyson.tapestry.webjars;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.ioc.LoggerSource;
import org.slf4j.Logger;
import org.webjars.MultipleMatchesException;
import org.webjars.WebJarAssetLocator;

public class AssetPathResolverImpl implements AssetPathResolver {

  private static final Pattern versionVariablePattern = Pattern
      .compile(Pattern.quote(WebjarsResource.VERSION_VARIABLE));

  private static final Pattern fullPathPattern = Pattern
      .compile("^" + Pattern.quote(WebJarAssetLocator.WEBJARS_PATH_PREFIX) + "/([^/]+)/([^/]+)(/.+)$");

  private final WebJarAssetLocator webJarAssetLocator;

  private final Logger logger;

  private final Map<String, String> webjars;

  public AssetPathResolverImpl(final WebJarAssetLocator webJarAssetLocator, final LoggerSource loggerSource) {
    this.webJarAssetLocator = webJarAssetLocator;
    this.logger = loggerSource.getLogger(AssetPathResolverImpl.class);
    this.webjars = Collections.unmodifiableMap(webJarAssetLocator.getWebJars());

  }

  private String resolveVersionInPath(final String path) {
    int indexOfVersionVariable = path.indexOf(WebjarsResource.VERSION_VARIABLE);

    if (indexOfVersionVariable >= 0) {
      List<String> candidates = new LinkedList<>();
      for (Entry<String, String> e : webjars.entrySet()) {
        String webjar = e.getKey();
        String version = e.getValue();
        StringBuilder sb = new StringBuilder(path);
        sb.replace(indexOfVersionVariable, indexOfVersionVariable + WebjarsResource.VERSION_VARIABLE.length(), version);
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
  public String resolve(final String path) {

    String p = path;
    final String assetPath = p;
    int indexOfColon = p.indexOf(':');
    try {
      if (indexOfColon < 0) {
        p = resolveVersionInPath(p);
        String fullPath = webJarAssetLocator.getFullPath(p);
        if (logger.isWarnEnabled()) {

          Matcher m = fullPathPattern.matcher(fullPath);
          if (m.find()) {
            String webjar = m.group(1);
            String pathInWebjar = m.group(3);
            String pathSuggestion = "webjars:" + webjar + ':' + WebjarsResource.VERSION_VARIABLE + pathInWebjar;
            logger.warn(
                "Resolved unspecific asset path \"{}\". For optimal performance, the asset location should be absolute, including WebJar name the version number or the version number placeholder, e.g \"{}\".",
                assetPath, pathSuggestion);
          }
        }

        return fullPath;
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
          String prefix = WebJarAssetLocator.WEBJARS_PATH_PREFIX + "/" + webjar + "/";
          if (versionNumberAtStart) {
            return prefix + p;
          } else if (p.startsWith(maybeVersion)) {
            return prefix + p;
          } else {
            String fullPath = webJarAssetLocator.getFullPath(webjar, p);
            if (logger.isWarnEnabled()) {
              String pathSuggestion = "webjars:" + webjar + ':' + WebjarsResource.VERSION_VARIABLE
                  + fullPath.substring(prefix.length() + maybeVersion.length());
              logger.warn(
                  "Resolved unspecific asset path \"{}\". For optimal performance, the asset location should be absolute, including WebJar name the version number or the version number placeholder, e.g \"{}\".",
                  assetPath, pathSuggestion);
            }
            return fullPath;
          }

        } else {
          return path;

        }

      }

    } catch (MultipleMatchesException e) {
      throw e;
    } catch (IllegalArgumentException e) {
      return path;

    }

  }

}
