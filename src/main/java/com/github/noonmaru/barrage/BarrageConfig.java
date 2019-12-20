package com.github.noonmaru.barrage;

import com.github.noonmaru.math.Vector;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Nemo
 */
public class BarrageConfig
{
    public static double bulletPosY;

    public static int fireTick;

    public static double bulletSize;

    public static double bulletDamage;

    public static Vector stardiumPos;

    public static double stadiumSize;

    public static double bulletSpeed;

    public static void load(ConfigurationSection config)
    {
        fireTick = config.getInt("fire-tick");
        bulletSize = config.getDouble("bullet-size");
        bulletDamage = config.getDouble("bullet-damage");
        stardiumPos = getVector(config.getConfigurationSection("stadium-pos"));
        stadiumSize = config.getDouble("stadium-size");
        bulletSpeed = config.getDouble("bullet-speed");
    }

    private static Vector getVector(ConfigurationSection config)
    {
        return new Vector(config.getDouble("x"), config.getDouble("y"), config.getDouble("z"));
    }
}
