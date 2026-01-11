package net.woolf.bella.models;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BackpackModel {

  @Nonnull
  private final String uuid;
  @Nonnull
  private final String inventoryName;
  @Nullable
  private final String itemData;

  public BackpackModel(
      @Nonnull String uuid,
      @Nonnull String inventoryName,
      @Nullable String itemData
  ) {
    this.uuid = uuid;
    this.inventoryName = inventoryName;
    this.itemData = itemData;
  }

  @Nonnull
  public String getUuid() {
    return uuid;
  }

  @Nonnull
  public String getInventoryName() {
    return inventoryName;
  }

  @Nullable
  public String getItemData() {
    return itemData;
  }
}
