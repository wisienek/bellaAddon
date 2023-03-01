package net.woolf.bella.utils;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class InventorySerializer {

  @SuppressWarnings( "unchecked" )
  public static String InventoryToString (
      Inventory invInventory
  ) {
    JSONObject inventoryObject = new JSONObject();
    JSONArray items = new JSONArray();

    for ( int i = 0; i < invInventory.getSize(); i++ ) {
      ItemStack is = invInventory.getItem(i);

      if ( is != null && is.getType() != Material.AIR ) {
        JSONObject jo = new JSONObject();
        jo.put("slot", i);

        String nbts = NBTItem.convertItemtoNBT(is).toString();
        jo.put("nbt", nbts);

        items.add(jo);
      }
    }

    inventoryObject.put("items", items);
    inventoryObject.put("slots", invInventory.getSize());

    return inventoryObject.toString();
  }

  public static Inventory StringToInventory (
      @NotNull String invString
  ) throws ParseException {
    if ( invString.length() == 0 ) return null;

    JSONParser parser = new JSONParser();

    JSONObject inventoryObject = (JSONObject) parser.parse(invString);
    JSONArray items = (JSONArray) inventoryObject.get("items");

    int size = ( (Long) inventoryObject.get("slots") ).intValue();

    Inventory deserializedInventory = Bukkit.getServer().createInventory(null, size);

    for ( Object item : items ) {
      JSONObject currentItem = (JSONObject) item;
      int pos = ( (Long) currentItem.get("slot") ).intValue();
      String nbtString = (String) currentItem.get("nbt");

      ItemStack is = NBTItem.convertNBTtoItem(new NBTContainer(nbtString));

      deserializedInventory.setItem(pos, is);
    }

    return deserializedInventory;
  }
}
