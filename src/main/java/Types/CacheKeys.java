package Types;

public enum CacheKeys {

  ChatCacheKey("chat-cache-key");

  private final String text;

  CacheKeys (
      final String text
  ) {
    this.text = text;
  }

  @Override
  public String toString () {
    return text;
  }
}
