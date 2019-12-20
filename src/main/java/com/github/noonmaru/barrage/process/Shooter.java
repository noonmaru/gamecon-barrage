package com.github.noonmaru.barrage.process;

import com.github.noonmaru.barrage.BarrageConfig;
import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.item.TapItemStack;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Nemo
 */
public class Shooter extends BarragePlayer
{
    private final TapItemStack bulletItem;

    private int ticks;

    public Shooter(BarrageProcess process, Player player, TapItemStack bulletItem)
    {
        super(process, player);

        this.bulletItem = bulletItem;
    }

    @Override
    public void onUpdate()
    {
        if (++ticks % BarrageConfig.fireTick == 0)
            fire();
    }

    public void fire()
    {
        Location loc = player.getLocation();
        double rotY = Math.toRadians(loc.getYaw());

        double vectorX = -Math.sin(rotY);
        double vectorZ = Math.cos(rotY);

            Vector pos = new Vector(loc.getX(), BarrageConfig.stardiumPos.y, loc.getZ());
            Vector velocity = new Vector(vectorX, 0, vectorZ).multiply(BarrageConfig.bulletSpeed);

            Bullet bullet = new Bullet(this.process, loc.getWorld(), pos, velocity, this.bulletItem, this);

            process.addBullet(bullet);
    }
}
