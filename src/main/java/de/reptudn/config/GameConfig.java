package de.reptudn.config;

public class GameConfig {

    public static final int MAX_PLAYERS = 20;
    public static final int WORLD_HEIGHT = 256;
    public static final int WORLD_SEA_FLOOR_HEIGHT = 40;
    public static final int SEA_LEVEL = 63;

    // Terrain generation settings
    public static final int MIN_MOUNTAIN_HEIGHT = 2;
    public static final int MAX_MOUNTAIN_HEIGHT = 16;
    public static final int SEA_HEIGHT = 50; // Water level height

    public static final double SUBMARINE_SPEED = 0.15;
    public static final double TORPEDO_SPEED = 0.4;
    public static final double WATER_DRAG = 0.85;
}
