package de.reptudn.world;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;
import de.reptudn.config.GameConfig;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;

public class WorldGeneratorUnderwater implements Generator {

    final JNoise noise;


    public WorldGeneratorUnderwater(long seed) {
        super();
        noise = JNoise.newBuilder()
                .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(seed).build())
                .scale(0.01) // Adjust scale for terrain features
                .build();
    }

    public WorldGeneratorUnderwater() {
        super();
        final long seed = System.currentTimeMillis();
        noise = JNoise.newBuilder()
                .fastSimplex(FastSimplexNoiseGenerator.newBuilder().setSeed(seed).build())
                .scale(0.01) // Adjust scale for terrain features
                .build();
    }

    @Override
    public void generate(GenerationUnit unit) {
        Point start = unit.absoluteStart();
        for (int x = 0; x < unit.size().x(); x++) {
            for (int z = 0; z < unit.size().z(); z++) {
                Point bottom = start.add(x, 0, z);
                double random = Math.random();

                synchronized (noise) { // Synchronization is necessary for JNoise
                    // Generate mountain height using GameConfig values
                    double heightRange = GameConfig.MAX_MOUNTAIN_HEIGHT - GameConfig.MIN_MOUNTAIN_HEIGHT;
                    double height = noise.evaluateNoise(bottom.x(), bottom.z()) * (heightRange / 2) + (heightRange / 2) + GameConfig.MIN_MOUNTAIN_HEIGHT;
                    height = Math.max(GameConfig.MIN_MOUNTAIN_HEIGHT, Math.min(GameConfig.MAX_MOUNTAIN_HEIGHT, height));

                    // Place bedrock at y=0 (bottom layer)
                    unit.modifier().setBlock(bottom, Block.BEDROCK);

                    // Fill stone mountains from y=1 (above bedrock) to calculated height
                    if (height > 1) {
                        if (random < 0.1) {
                            unit.modifier().fill(bottom.add(0, 1, 0), bottom.add(1, height, 1), Block.DEEPSLATE);
                        } else if (random < 0.2) {
                            unit.modifier().fill(bottom.add(0, 1, 0), bottom.add(1, height, 1), Block.ANDESITE);
                        } else {
                            unit.modifier().fill(bottom.add(0, 1, 0), bottom.add(1, height, 1), random < 0.4 ? Block.MOSSY_COBBLESTONE : Block.STONE);
                        }
                    }

                    // Fill with water from mountain surface to configured sea height
                    if (height < GameConfig.SEA_HEIGHT) {
                        unit.modifier().fill(bottom.add(0, height, 0), bottom.add(1, GameConfig.SEA_HEIGHT, 1), Block.WATER);
                    }
                }
            }
        }
    }
}
