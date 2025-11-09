package de.reptudn.game.Weapons;

import net.minestom.server.entity.Player;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SubmarineGun extends Weapon {

    public SubmarineGun() {
        super("Submarine Gun", 30, 20.0, 50.0, 0.5);
    }

    public void fireProjectile(Player player) {
        if (!canShoot()) {
            if (ammo <= 0) {
                player.sendMessage(Component.text("Keine Munition!", NamedTextColor.RED));
            } else {
                player.sendMessage(Component.text("Waffe lädt noch...", NamedTextColor.YELLOW));
            }
            return;
        }

        // Shoot the weapon
        shoot();

        // Create and launch projectile
        Projectile projectile = new Projectile(player, damage, 2.0, range);
        projectile.launch(player.getInstance());

        // Play torpedo launch sound effect
        player.playSound(Sound.sound(Key.key("entity.firework_rocket.launch"), Sound.Source.PLAYER, 1.5f, 0.6f));

        // Send feedback to player
        player.sendMessage(Component.text("Geschossen! Munition: " + ammo + "/" + maxAmmo, NamedTextColor.GREEN));
    }

    public void reload() {
        ammo = maxAmmo;
    }

    public int getAmmo() {
        return ammo;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public String getName() {
        return name;
    }
}
