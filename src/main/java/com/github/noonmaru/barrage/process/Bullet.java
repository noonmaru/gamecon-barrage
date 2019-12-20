package com.github.noonmaru.barrage.process;

import com.github.noonmaru.barrage.BarrageConfig;
import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.Particle;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.entity.TapArmorStand;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.math.BoundingBox;
import com.github.noonmaru.tap.math.RayTraceResult;
import com.github.noonmaru.tap.packet.Packet;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

/**
 * @author Nemo
 */
public class Bullet
{
    private final BarrageProcess process;

    private final World world;

    private final Vector pos;

    private final Vector move;

    private final TapItemStack bulletItem;

    private final Shooter shooter;

    private final TapArmorStand bullet;

    private final Queue<RayTracePath> paths = new ArrayDeque<>();

    private boolean isInBox;

    private boolean removed;

    private boolean dead;

    private int ticks;

    private RayTracePath lastPath;

    public Bullet(BarrageProcess process, World world, Vector pos, Vector move, TapItemStack bulletItem, Shooter shooter)
    {
        this.process = process;
        this.world = world;
        this.pos = pos;
        this.move = move;
        this.bulletItem = bulletItem;
        this.shooter = shooter;

        this.bullet = Tap.ENTITY.createEntity(ArmorStand.class);
        this.bullet.setInvisible(true);
        this.bullet.setPosition(pos.x, pos.y, pos.z);
    }

    public void spawnTo(Collection<? extends Player> players)
    {
        ArmorStand entity = bullet.getBukkitEntity();
        Packet.ENTITY.spawnMob(entity).sendTo(players);
        Packet.ENTITY.metadata(entity).sendTo(players);
    }

    public void updateEquipmentTo(Collection<? extends Player> players)
    {
        Packet.ENTITY.equipment(bullet.getId(), EquipmentSlot.HEAD, bulletItem).sendTo(players);
    }

    public void onUpdate()
    {
        if (dead)
            return;

        if (++ticks == 2) // 0.1초 뒤 업데이트
        {
            updateEquipmentTo(Bukkit.getOnlinePlayers());
        }

        if (!removed)
        {
            double yOffset = BarrageConfig.bulletSize / 2;
            Vector from = pos.copy();
            from.y += yOffset;
            Vector to = from.copy().add(move);
            this.pos.add(move);
            this.box = null;

            Packet.ENTITY.relativeMove(bullet.getId(), move.x, move.y, move.z, false).sendAll();

            this.paths.offer(new RayTracePath(from, to, ticks + 2));

            BoundingBox stadiumBox = process.getBox();
            boolean isInSide = stadiumBox.isInside(to);

            if (isInSide)
            {
                isInBox = true;
            }
            else if (isInBox) //바깥으로 나갈때
            {
                removed = true;
            }
        }

        while (true)
        {
            RayTracePath path = paths.peek();

            if (path != null)
            {
                if (path.calculateTick <= ticks)
                {
                    lastPath = path;
                    paths.remove();

                    Avoider found = null;
                    double distance = 0.0D;

                    for (Avoider avoider : process.getSurvivors())
                    {
                        if (!avoider.isOnline())
                            continue;

                        RayTraceResult rayTraceResult = avoider.getTapPlayer().getBoundingBox().expand(BarrageConfig.bulletSize / 2).calculateRayTrace(path.from, path.to);

                        if (rayTraceResult != null)
                        {
                            double curDistance = path.from.distance(rayTraceResult.getY(), rayTraceResult.getY(), rayTraceResult.getZ());
                            if (distance == 0.0D || distance < curDistance)
                            {
                                distance = curDistance;
                                found = avoider;
                            }
                        }
                    }

                    if (found != null)
                    {
                        Player player = found.getPlayer();
                        player.setNoDamageTicks(0);
                        player.damage(BarrageConfig.bulletDamage);
                        Vector v = path.to.subtract(path.from).normalize();
                        player.setVelocity(new org.bukkit.util.Vector(v.x, v.y, v.z));
                        dead = true;
                        destroy();
                    }

                    continue;
                }
            }
            else
            {
                if (removed)
                {
                    destroy();
                    dead = true;
                }
            }

            break;
        }

        if (pos.distance(BarrageConfig.stardiumPos) > BarrageConfig.stadiumSize * 3 / 4)
        {
            destroy();
            dead = true;
        }

    }

    public World getWorld()
    {
        return world;
    }

    public Vector getPos()
    {
        return pos;
    }

    public Vector getMove()
    {
        return move;
    }

    public TapItemStack getBulletItem()
    {
        return bulletItem;
    }

    public Shooter getShooter()
    {
        return shooter;
    }

    public boolean isDead()
    {
        return dead;
    }

    public void destroy()
    {
        Packet.ENTITY.destroy(bullet.getId()).sendAll();
    }

    public void remove()
    {
        Vector pos = lastPath == null ? this.pos : lastPath.to;

        Packet.EFFECT.particle(Particle.END_ROD, (float) pos.x, (float) (pos.y + BarrageConfig.bulletSize / 2), (float) pos.z, 0.0F, 0.0F, 0.0F, 0.05F, 10).sendAll();
        destroy();
        dead = true;
    }

    private BoundingBox box;

    public BoundingBox getBox()
    {
        if (box == null)
        {
            double r = BarrageConfig.bulletSize / 2;
            Vector pos = lastPath == null ? this.pos : lastPath.to;
            box = Tap.MATH.newBoundingBox(pos.x - r, pos.y, pos.z - r, pos.x + r, pos.y + BarrageConfig.bulletSize, pos.z + r);
        }

        return box;
    }
}
