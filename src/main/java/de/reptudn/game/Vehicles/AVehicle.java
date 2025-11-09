package de.reptudn.game.Vehicles;

public abstract class AVehicle {

    // General
    protected int id;

    // Health Stuff
    protected int health;
    protected int maxHealth;

    // Movement Stuff
    protected float speed;
    protected float acceleration;
    protected float turnSpeed;
    protected float drag;

   public AVehicle(int id, int health, int maxHealth) {
        this.id = id;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public AVehicle(int id) {
        this.id = id;
        this.health = 100;
        this.maxHealth = 100;
    }

    protected boolean alive() {
        return health > 0;
    };

    protected void heal(int amount) {
        health += amount;
        if (health > maxHealth) {
            health = maxHealth;
        }
    };

    protected void damage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
    };
}
