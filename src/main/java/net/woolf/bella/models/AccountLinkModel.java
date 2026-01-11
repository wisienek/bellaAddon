package net.woolf.bella.models;

import javax.annotation.Nonnull;

public class AccountLinkModel {

  @Nonnull
  private final String discordId;
  @Nonnull
  private final String uuid;
  @Nonnull
  private final String playerName;

  public AccountLinkModel(
      @Nonnull String discordId,
      @Nonnull String uuid,
      @Nonnull String playerName
  ) {
    this.discordId = discordId;
    this.uuid = uuid;
    this.playerName = playerName;
  }

  @Nonnull
  public String getDiscordId() {
    return discordId;
  }

  @Nonnull
  public String getUuid() {
    return uuid;
  }

  @Nonnull
  public String getPlayerName() {
    return playerName;
  }
}
