package de.reptudn.game.Weapons;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;

public class WeaponManager {
    private static final Map<Player, Weapon> playerWeapons = new HashMap<>();

    public static void giveWeapon(Player player, Weapon weapon) {
        playerWeapons.put(player, weapon);

        // Give weapon item to player
        ItemStack weaponItem = ItemStack.of(Material.CROSSBOW)
                .withCustomName(Component.text(weapon.getName(), NamedTextColor.GOLD))
                .withLore(
                    Component.text("Schaden: " + weapon.getDamage(), NamedTextColor.GRAY),
                    Component.text("Reichweite: " + weapon.getRange(), NamedTextColor.GRAY),
                    Component.text("Munition: " + weapon.getAmmo() + "/" + weapon.getMaxAmmo(), NamedTextColor.GRAY),
                    Component.text("", NamedTextColor.GRAY),
                    Component.text("Linksklick zum Schießen", NamedTextColor.YELLOW),
                    Component.text("Shift+Rechtsklick zum Nachladen", NamedTextColor.YELLOW)
                )
                .withTag(net.minestom.server.tag.Tag.Integer("CustomModelData"), 1);

        player.getInventory().setItemStack(0, weaponItem);
        player.sendMessage(Component.text("Du hast eine " + weapon.getName() + " erhalten!", NamedTextColor.GREEN));
    }

    public static Weapon getWeapon(Player player) {
        return playerWeapons.get(player);
    }

    public static boolean hasWeapon(Player player) {
        return playerWeapons.containsKey(player);
    }

    public static void removeWeapon(Player player) {
        playerWeapons.remove(player);
    }

    public static void updateWeaponItem(Player player) {
        Weapon weapon = getWeapon(player);
        if (weapon == null) return;

        ItemStack weaponItem = ItemStack.of(Material.CROSSBOW)
                .withCustomName(Component.text(weapon.getName(), NamedTextColor.GOLD))
                .withLore(
                    Component.text("Schaden: " + weapon.getDamage(), NamedTextColor.GRAY),
                    Component.text("Reichweite: " + weapon.getRange(), NamedTextColor.GRAY),
                    Component.text("Munition: " + weapon.getAmmo() + "/" + weapon.getMaxAmmo(), NamedTextColor.GRAY),
                    Component.text("", NamedTextColor.GRAY),
                    Component.text("Linksklick zum Schießen", NamedTextColor.YELLOW),
                    Component.text("Shift+Rechtsklick zum Nachladen", NamedTextColor.YELLOW)
                );

        // Force set the weapon item to ensure it stays as crossbow
        player.getInventory().setItemStack(0, weaponItem);
    }
}
