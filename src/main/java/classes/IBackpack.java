package classes;

import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IBackpack extends InventoryHolder {

	void open(
			@NotNull Player player,
			boolean editable,
			@Nullable String title
	);

	boolean isOpen();

	int getSize();

	boolean hasChanged();

	void setChanged();

	void save();

	void clear();

	void drop(
			Location location
	);

	ItemStack getBagItem();

	void setBagItem(
			@NotNull ItemStack item
	);

	default @Nullable ItemStack addItem(
			ItemStack stack
	) {
		Map<Integer, ItemStack> left = this.addItems( stack );
		if ( left.isEmpty() )
			return null;

		return left.get( 0 );
	}

	default @NotNull Map<Integer, ItemStack> addItems(
			ItemStack... itemStacks
	) {
		setChanged();
		return getInventory().addItem( itemStacks );
	}

	static boolean isBackPack(
			@Nullable Inventory inventory
	) {
		return inventory instanceof IBackpack;
	}

	void open(
			@NotNull Player player,
			boolean editable
	);
}
