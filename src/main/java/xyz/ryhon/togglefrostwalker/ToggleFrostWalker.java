package xyz.ryhon.togglefrostwalker;

import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

public class ToggleFrostWalker extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		PaperLib.suggestPaper(this);

		getServer().getPluginManager().registerEvents(this, this);

		getCommand("togglefrostwalker").setExecutor(new CommandExecutor() {
			public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
					@NotNull String[] args) {
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command may only be ran by players!");
					return false;
				}

				Player p = (Player) sender;
				toggleFWDisabled(p);
				if (isFWDisabled(p))
					p.sendMessage("Frost Walker is now " + ChatColor.RED + "DISABLED");
				else
					p.sendMessage("Frost Walker is now " + ChatColor.GREEN + "ENABLED");

				return true;
			}
		});

		getCommand("togglefrostwalkersneaktoggle").setExecutor(new CommandExecutor() {
			public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
					@NotNull String[] args) {
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command may only be ran by players!");
					return false;
				}

				Player p = (Player) sender;
				toggleSneakToggleDisabled(p);
				if (isSneakToggleDisabled(p))
					p.sendMessage("Frost Walker sneak toggle is now " + ChatColor.RED + "DISABLED");
				else
					p.sendMessage("Frost Walker sneak toggle is now " + ChatColor.GREEN + "ENABLED");

				return true;
			}
		});
	}

	@EventHandler
	public void on(EntityBlockFormEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (isFWDisabled(p)) {
				e.setCancelled(true);
				return;
			}
		}
	}

	HashMap<Player, int[]> PlayerCrouches = new HashMap<>();

	@EventHandler
	public void on(PlayerToggleSneakEvent e) {
		Player p = e.getPlayer();
		if(isSneakToggleDisabled(p)) return;

		if (!e.isSneaking())
			return;

		if (!PlayerCrouches.containsKey(p))
			PlayerCrouches.put(p, new int[] { -1, -1, -1 });

		int[] l = PlayerCrouches.get(p);
		l = new int[] { l[1], l[2], Bukkit.getServer().getCurrentTick() };
		PlayerCrouches.put(p, l);

		if (ArrayUtils.indexOf(l, -1) != -1)
			return;

		if (Math.max(l[1] - l[0], l[2] - l[1]) <= 10) {
			
			PlayerCrouches.put(p, new int[] { -1, -1, -1 });
			toggleFWDisabled(p);

			if (isFWDisabled(p))
			{
				p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1f);
				p.sendActionBar(ChatColor.GRAY + "/tfwst - " + ChatColor.RESET + "Frost Walker is now " + ChatColor.RED + "DISABLED" + ChatColor.DARK_GRAY);
			}
			else
			{
				p.playSound(p.getLocation(), Sound.BLOCK_GLASS_PLACE, 0.5f, 1f);
				p.sendActionBar(ChatColor.GRAY + "/tfwst - " + ChatColor.RESET + "Frost Walker is now " + ChatColor.GREEN + "ENABLED");
			}
		}
	}

	@EventHandler
	public void on(PlayerQuitEvent e) {
		PlayerCrouches.remove(e.getPlayer());
	}

	static NamespacedKey FWDisabledKey = new NamespacedKey("togglefrostwalker", "disabled");
	static NamespacedKey FWSneakToggleDisabledKey = new NamespacedKey("togglefrostwalker", "sneaktoggledisabled");

	boolean isFWDisabled(Player p) {
		PersistentDataContainer psc = p.getPersistentDataContainer();
		if (!psc.has(FWDisabledKey, PersistentDataType.BYTE))
			return false;

		return psc.get(FWDisabledKey, PersistentDataType.BYTE) != 0;
	}

	void setFWDisabled(Player p, boolean disabled) {
		PersistentDataContainer psc = p.getPersistentDataContainer();
		psc.set(FWDisabledKey, PersistentDataType.BYTE, disabled ? (byte) 1 : (byte) 0);
	}

	void toggleFWDisabled(Player p) {
		setFWDisabled(p, !isFWDisabled(p));
	}

	boolean isSneakToggleDisabled(Player p) {
		PersistentDataContainer psc = p.getPersistentDataContainer();
		if (!psc.has(FWSneakToggleDisabledKey, PersistentDataType.BYTE))
			return false;

		return psc.get(FWSneakToggleDisabledKey, PersistentDataType.BYTE) != 0;
	}

	void setSneakToggleDisabled(Player p, boolean disabled) {
		PersistentDataContainer psc = p.getPersistentDataContainer();
		psc.set(FWSneakToggleDisabledKey, PersistentDataType.BYTE, disabled ? (byte) 1 : (byte) 0);
	}

	void toggleSneakToggleDisabled(Player p) {
		setSneakToggleDisabled(p, !isSneakToggleDisabled(p));
	}
}
