package de.reptudn.game.Weapons;

public abstract class Weapon {

    protected int ammo;
    protected int maxAmmo;
    protected double damage;
    protected double range;
    protected double cooldown; // in seconds

    protected String name;

    public Weapon(String name, int maxAmmo, double damage, double range, double cooldown) {
        this.name = name;
        this.maxAmmo = maxAmmo;
        this.ammo = maxAmmo;
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
    }

    private long lastShotTime = 0;

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        return ammo > 0 && (currentTime - lastShotTime) >= (cooldown * 1000);
    }

    public void shoot(){
        if (!canShoot())
            throw new IllegalStateException("Cannot shoot: either out of ammo or weapon is cooling down.");

        ammo--;
        lastShotTime = System.currentTimeMillis();
    }

    // Getter methods
    public int getAmmo() { return ammo; }
    public int getMaxAmmo() { return maxAmmo; }
    public double getDamage() { return damage; }
    public double getRange() { return range; }
    public double getCooldown() { return cooldown; }
    public String getName() { return name; }

}
