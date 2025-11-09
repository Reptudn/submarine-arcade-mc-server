package de.reptudn.commands;

import de.reptudn.game.Weapons.SubmarineGun;
import de.reptudn.game.Weapons.Weapon;
import de.reptudn.game.Weapons.WeaponManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "r");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Nur Spieler können Waffen nachladen.");
                return;
            }

            if (!WeaponManager.hasWeapon(player)) {
                player.sendMessage(Component.text("Du hast keine Waffe!", NamedTextColor.RED));
                return;
            }

            Weapon weapon = WeaponManager.getWeapon(player);
            if (weapon instanceof SubmarineGun submarineGun) {
                submarineGun.reload();
                WeaponManager.updateWeaponItem(player);
                player.sendMessage(Component.text("Waffe nachgeladen! Munition: " +
                    submarineGun.getAmmo() + "/" + submarineGun.getMaxAmmo(), NamedTextColor.GREEN));
            }
        });
    }
}
