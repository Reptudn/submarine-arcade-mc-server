package de.reptudn.game.Weapons;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.key.Key;

public class Projectile extends Entity {
    private final double damage;
    private final Player shooter;
    private final Vec direction;
    private final double speed;
    private final double maxRange;
    private double traveledDistance = 0;

    public Projectile(Player shooter, double damage, double speed, double maxRange) {
        super(EntityType.WITHER_SKULL);
        this.shooter = shooter;
        this.damage = damage;
        this.speed = speed;
        this.maxRange = maxRange;
        this.direction = shooter.getPosition().direction();
    }

    public void launch(Instance instance) {
        Pos startPos = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        setInstance(instance, startPos);

        // Move projectile every tick using submitTask for repeating tasks
        scheduler().submitTask(() -> {
            if (!isAlive()) {
                return TaskSchedule.stop();
            }

            // Check if max range reached - explode!
            if (traveledDistance >= maxRange) {
                explode(instance, getPosition());
                return TaskSchedule.stop();
            }

            Pos currentPos = getPosition();
            Block currentBlock = instance.getBlock(currentPos);

            Vec movement;
            Pos newPos;

            // Check if projectile is in water
            if (currentBlock == Block.WATER) {
                // Normal movement in water - torpedo behavior
                movement = direction.mul(speed);
                newPos = currentPos.add(movement);
                traveledDistance += speed;

                // Create underwater particle trail effects
                createUnderwaterParticleTrail(instance, currentPos);
            } else {
                // Not in water - fall straight down until hitting water or solid block
                movement = new Vec(0, -speed * 2, 0); // Fall faster than normal movement
                newPos = currentPos.add(movement);

                // Create falling particle effects (no bubbles, just fire/smoke)
                createFallingParticleTrail(instance, currentPos);

                // Check what we're falling into
                Block blockBelow = instance.getBlock(newPos);

                // If we hit a solid block (not air, not water) while falling - explode
                if (!blockBelow.isAir() && blockBelow != Block.WATER) {
                    explode(instance, currentPos);
                    return TaskSchedule.stop();
                }
            }

            teleport(newPos);

            // Check for collisions with players
            for (Player player : instance.getPlayers()) {
                if (player != shooter && player.getDistanceSquared(this) < 2.0) {
                    // Hit player - explode on impact
                    player.damage(null, (float) damage);
                    player.sendMessage("Du wurdest von " + shooter.getUsername() + " getroffen!");
                    shooter.sendMessage("Du hast " + player.getUsername() + " getroffen!");
                    explode(instance, getPosition());
                    return TaskSchedule.stop();
                }
            }

            return TaskSchedule.tick(1);
        });
    }

    private boolean isAlive() {
        return this.getInstance() != null;
    }

    private void createUnderwaterParticleTrail(Instance instance, Pos position) {
        // Fire particles (less visible underwater)
        ParticlePacket fireParticle = new ParticlePacket(
            Particle.FLAME,
            position.x(), position.y(), position.z(),
            0.1f, 0.1f, 0.1f, // smaller offset underwater
            0.01f, // extra data (speed)
            2 // fewer particles underwater
        );

        // Water bubble particles (main effect underwater)
        ParticlePacket bubbleParticle = new ParticlePacket(
            Particle.BUBBLE,
            position.x(), position.y(), position.z(),
            0.5f, 0.5f, 0.5f, // larger offset for bubble trail
            0.1f, // faster bubbles
            8 // more bubbles underwater
        );

        // Bubble column particles for torpedo wake
        ParticlePacket wakeParticle = new ParticlePacket(
            Particle.BUBBLE_COLUMN_UP,
            position.x(), position.y(), position.z(),
            0.3f, 0.3f, 0.3f, // offset
            0.05f, // extra data
            3 // particle count
        );

        // Send particles to all nearby players
        for (Player player : instance.getPlayers()) {
            if (player.getPosition().distance(position) <= 64) {
                player.sendPacket(fireParticle);
                player.sendPacket(bubbleParticle);
                player.sendPacket(wakeParticle);
            }
        }
    }

    private void createFallingParticleTrail(Instance instance, Pos position) {
        // Fire particles (more visible in air)
        ParticlePacket fireParticle = new ParticlePacket(
            Particle.FLAME,
            position.x(), position.y(), position.z(),
            0.3f, 0.3f, 0.3f, // larger offset in air
            0.02f, // extra data (speed)
            5 // more fire particles in air
        );

        // Smoke particles (only in air)
        ParticlePacket smokeParticle = new ParticlePacket(
            Particle.LARGE_SMOKE,
            position.x(), position.y(), position.z(),
            0.4f, 0.4f, 0.4f, // offset
            0.03f, // extra data (speed)
            4 // particle count
        );

        // Send particles to all nearby players
        for (Player player : instance.getPlayers()) {
            if (player.getPosition().distance(position) <= 64) {
                player.sendPacket(fireParticle);
                player.sendPacket(smokeParticle);
            }
        }
    }

    private void explode(Instance instance, Pos position) {
        // Create explosion particles
        ParticlePacket explosionParticle = new ParticlePacket(
            Particle.EXPLOSION,
            position.x(), position.y(), position.z(),
            2.0f, 2.0f, 2.0f, // offset
            0.1f, // extra data
            15 // particle count
        );

        // Create additional fire particles for the explosion
        ParticlePacket explosionFireParticle = new ParticlePacket(
            Particle.LAVA,
            position.x(), position.y(), position.z(),
            3.0f, 3.0f, 3.0f, // offset
            0.2f, // extra data
            20 // particle count
        );

        // Play explosion sound
        Sound explosionSound = Sound.sound(Key.key("entity.generic.explode"), Sound.Source.HOSTILE, 2.0f, 0.8f);

        // Send explosion effects to all nearby players
        for (Player player : instance.getPlayers()) {
            if (player.getPosition().distance(position) <= 100) {
                player.sendPacket(explosionParticle);
                player.sendPacket(explosionFireParticle);
                player.playSound(explosionSound);
            }
        }

        // Damage nearby players from explosion
        double explosionRadius = 5.0;
        for (Player player : instance.getPlayers()) {
            double distance = player.getPosition().distance(position);
            if (distance <= explosionRadius && player != shooter) {
                // Calculate damage based on distance (closer = more damage)
                float explosionDamage = (float) (damage * 0.5 * (1.0 - (distance / explosionRadius)));
                if (explosionDamage > 0) {
                    player.damage(null, explosionDamage);
                    player.sendMessage("Du wurdest von einer Explosion getroffen!");
                }
            }
        }

        // Remove the projectile
        remove();
    }
}
