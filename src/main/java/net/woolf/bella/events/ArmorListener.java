package net.woolf.bella.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

public class ArmorListener implements Listener {

    private final Plugin plugin;

    public ArmorListener(
            Plugin plugin
    ) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(
            final InventoryClickEvent e
    ) {
        if ( e.getInventory() instanceof PlayerInventory ) {
            PlayerInventory inv = (PlayerInventory) e.getInventory();
            boolean shift = e.getClick() == ClickType.SHIFT_LEFT
                    || e.getClick() == ClickType.SHIFT_RIGHT;
            if ( e.getSlotType() == InventoryType.SlotType.ARMOR ) {
                ArmorType newArmorType = ArmorType.matchType( e.getCursor() );
                if ( shift ) {
                    newArmorType = ArmorType.matchType( e.getCurrentItem() );
                }
                if ( newArmorType != null ) {
                    Player p = (Player) e.getWhoClicked();
                    ItemStack[] armorContents = inv.getArmorContents();
                    int armorSlot = getArmorSlotIndex( newArmorType );
                    ItemStack oldArmorPiece = armorSlot >= 0 && armorSlot < armorContents.length
                            ? armorContents[armorSlot]
                            : null;
                    ItemStack newArmorPiece = shift ? e.getCurrentItem() : e.getCursor();
                    callArmorEquipEvent( p, ArmorEquipEvent.EquipMethod.SHIFT_CLICK, newArmorType, oldArmorPiece, newArmorPiece );
                }
            } else if ( shift ) {
                ArmorType newArmorType = ArmorType.matchType( e.getCurrentItem() );
                if ( newArmorType != null ) {
                    Player p = (Player) e.getWhoClicked();
                    ItemStack[] armorContents = inv.getArmorContents();
                    int armorSlot = getArmorSlotIndex( newArmorType );
                    ItemStack oldArmorPiece = armorSlot >= 0 && armorSlot < armorContents.length
                            ? armorContents[armorSlot]
                            : null;
                    callArmorEquipEvent( p, ArmorEquipEvent.EquipMethod.SHIFT_CLICK, newArmorType, oldArmorPiece, e
                            .getCurrentItem() );
                }
            }
        }
    }

    private int getArmorSlotIndex(
            ArmorType type
    ) {
        switch ( type ) {
            case HELMET:
                return 3;

            case CHESTPLATE:
                return 2;

            case LEGGINGS:
                return 1;

            case BOOTS:
                return 0;

            default:
                return -1;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(
            final InventoryDragEvent e
    ) {
        if ( e.getInventory() instanceof PlayerInventory ) {
            PlayerInventory inv = (PlayerInventory) e.getInventory();
            for ( int slot : e.getRawSlots() ) {
                if ( slot >= 36 && slot <= 39 ) { // Armor slots in 1.20.1
                    ArmorType type = ArmorType.matchType( e.getOldCursor() );
                    if ( type != null ) {
                        Player p = (Player) e.getWhoClicked();
                        ItemStack[] armorContents = inv.getArmorContents();
                        int armorSlot = getArmorSlotIndex( type );
                        ItemStack oldArmorPiece = armorSlot >= 0 && armorSlot < armorContents.length
                                ? armorContents[armorSlot]
                                : null;
                        callArmorEquipEvent( p, ArmorEquipEvent.EquipMethod.DRAG, type, oldArmorPiece, e
                                .getOldCursor() );
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(
            final PlayerInteractEvent e
    ) {
        if ( e.getAction().name().contains( "RIGHT_CLICK" ) ) {
            Player p = e.getPlayer();
            ItemStack item = e.getItem();
            if ( item != null ) {
                ArmorType type = ArmorType.matchType( item );
                if ( type != null ) {
                    PlayerInventory inv = p.getInventory();
                    ItemStack[] armorContents = inv.getArmorContents();
                    int armorSlot = getArmorSlotIndex( type );
                    ItemStack oldArmorPiece = armorSlot >= 0 && armorSlot < armorContents.length
                            ? armorContents[armorSlot]
                            : null;
                    if ( ArmorType.isAirOrNull( oldArmorPiece ) ) {
                        callArmorEquipEvent( p, ArmorEquipEvent.EquipMethod.HOTBAR, type, oldArmorPiece, item );
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemBreak(
            final PlayerItemBreakEvent e
    ) {
        Player p = e.getPlayer();
        ItemStack brokenItem = e.getBrokenItem();
        ArmorType type = ArmorType.matchType( brokenItem );
        if ( type != null ) {
            PlayerInventory inv = p.getInventory();
            ItemStack[] armorContents = inv.getArmorContents();
            int armorSlot = getArmorSlotIndex( type );
            ItemStack oldArmorPiece = armorSlot >= 0 && armorSlot < armorContents.length
                    ? armorContents[armorSlot]
                    : null;
            if ( oldArmorPiece != null && oldArmorPiece.equals( brokenItem ) ) {
                callArmorEquipEvent( p, ArmorEquipEvent.EquipMethod.BROKE, type, oldArmorPiece, null );
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(
            final PlayerDeathEvent e
    ) {
        Player p = e.getEntity();
        for ( ItemStack i : p.getInventory().getArmorContents() ) {
            ArmorType type = ArmorType.matchType( i );
            if ( type != null ) {
                callArmorEquipEvent( p, ArmorEquipEvent.EquipMethod.DEATH, type, i, null );
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDispenseArmor(
            final BlockDispenseArmorEvent e
    ) {
        ArmorType type = ArmorType.matchType( e.getItem() );
        if ( type != null ) {
            org.bukkit.entity.LivingEntity target = e.getTargetEntity();
            if ( target instanceof Player ) {
                Player p = (Player) target;
                PlayerInventory inv = p.getInventory();
                ItemStack[] armorContents = inv.getArmorContents();
                int armorSlot = getArmorSlotIndex( type );
                ItemStack oldArmorPiece = armorSlot >= 0 && armorSlot < armorContents.length
                        ? armorContents[armorSlot]
                        : null;
                callArmorEquipEvent( p, ArmorEquipEvent.EquipMethod.DISPENSER, type, oldArmorPiece, e
                        .getItem() );
            }
        }
    }

    private void callArmorEquipEvent(
            final Player player,
            final ArmorEquipEvent.EquipMethod equipType,
            final ArmorType type,
            final ItemStack oldArmorPiece,
            final ItemStack newArmorPiece
    ) {
        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent( player, equipType, type,
                oldArmorPiece, newArmorPiece );
        Bukkit.getPluginManager().callEvent( armorEquipEvent );
        if ( armorEquipEvent.isCancelled() ) {
            return;
        }
    }
}
