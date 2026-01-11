package net.woolf.bella.utils;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class CacheUtils {

  private static final Map<String, Object> SimpleCache = new HashMap<>();

  public static void addToCache(
      @NotNull final String key,
      @NotNull final Object obj
  ) {
    if ( CacheUtils.hasKey( key ) )
      CacheUtils.removeFromCache( key );

    SimpleCache.put( key, obj );
  }

  public static void removeFromCache(
      @NotNull final String key
  ) {
    SimpleCache.remove( key );
  }

  public static boolean hasKey(
      @NotNull final String key
  ) {
    return SimpleCache.containsKey( key );
  }

  public static Object getObject(
      @NotNull final String key
  ) {
    return SimpleCache.get( key );
  }

}
