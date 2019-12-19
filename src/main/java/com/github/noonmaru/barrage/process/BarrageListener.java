package com.github.noonmaru.barrage.process;

import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.math.BoundingBox;
import com.github.noonmaru.tap.math.RayTraceResult;
import com.github.noonmaru.tap.math.RayTracer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Nemo
 */
public class BarrageListener implements Listener
{

    private final BarrageProcess process;

    public BarrageListener(BarrageProcess process)
    {
        this.process = process;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        process.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        process.onQuit(player);
    }

    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event)
    {
        Player player = event.getPlayer();
        BarragePlayer barragePlayer = process.getBarragePlayer(player);

        if (barragePlayer instanceof Avoider)
        {
            Avoider avoider = (Avoider) barragePlayer;

            if (avoider.isAlive())
            {
                Location loc = player.getEyeLocation();
                org.bukkit.util.Vector v = loc.getDirection().multiply(4.5);
                Vector from = new Vector(loc.getX(), loc.getY(), loc.getZ());
                Vector to = from.copy().add(v.getX(), v.getY(), v.getZ());
                RayTracer tracer = Tap.MATH.newRayTraceCalculator(from, to);

                Bullet found = null;
                double distance = 0.0D;

                for (Bullet bullet : process.getBullets())
                {
                    BoundingBox box = bullet.getBox();
                    RayTraceResult result = tracer.calculate(box.expand(1));
                    if (result != null)
                    {
                        double curDistance = from.distance(result.getY(), result.getY(), result.getZ());

                        if (distance == 0.0D || curDistance < distance)
                        {
                            distance = curDistance;
                            found = bullet;
                        }
                    }
                }

                if (found != null)
                    found.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        BarragePlayer barragePlayer = process.getBarragePlayer(player);

        if (barragePlayer instanceof Avoider)
        {
            Avoider avoider = (Avoider) barragePlayer;
            avoider.die();
            event.setDeathMessage(player.getName() + "님이 총알을 피하지 못했습니다.");
        }
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event)
    {
        Entity entity = event.getEntity();

        if (entity instanceof Player)
        {
            Player player = (Player) entity;
            BarragePlayer barragePlayer = process.getBarragePlayer(player);

            if (barragePlayer instanceof Avoider)
                event.setCancelled(true);
        }
    }
}
