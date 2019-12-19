package com.github.noonmaru.barrage.process;

import com.github.noonmaru.barrage.BarrageConfig;
import com.github.noonmaru.barrage.BarragePlugin;
import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.math.BoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

/**
 * @author Nemo
 */
public class BarrageProcess
{
    private final BarragePlugin plugin;

    private final BarrageListener listener;

    private final BukkitTask task;

    private final Map<UUID, BarragePlayer> players = new HashMap<>();

    private final Map<Player, BarragePlayer> onlinePlayers = new IdentityHashMap<>();

    private final Set<Avoider> survivors = new HashSet<>();

    private final BoundingBox box;

    private final List<Bullet> bullets = new ArrayList<>();

    public BarrageProcess(BarragePlugin plugin, Set<Player> survivors)
    {
        this.plugin = plugin;
        this.listener = new BarrageListener(this);
        plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, new BarrageScheduler(this), 0, 1);

        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("barrage");
        if (objective != null)
            objective.unregister();
        objective = scoreboard.registerNewObjective("barrage", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("     탄막 피하기    ");

        for (Player player : survivors)
        {
            Avoider avoider = new Avoider(this, player, objective.getScore(player.getName()));
            players.put(player.getUniqueId(), avoider);
            onlinePlayers.put(player, avoider);
            this.survivors.add(avoider);
        }

        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (onlinePlayers.containsKey(player))
                continue;

            GameMode mode = player.getGameMode();

            if (mode == GameMode.SPECTATOR || mode == GameMode.CREATIVE)
                continue;

            Shooter shooter = new Shooter(this, player, plugin.getBulletItem(player));
            players.put(player.getUniqueId(), shooter);
            onlinePlayers.put(player, shooter);
        }

        Vector pos = BarrageConfig.stardiumPos;
        double r = BarrageConfig.stadiumSize / 2;

        box = Tap.MATH.newBoundingBox(pos.x - r, 0, pos.z - r, pos.x + r, 256, pos.z + r);

        Location loc = new Location(Bukkit.getWorlds().get(0), pos.x, pos.y, pos.z);

        for (Avoider survivor : this.survivors)
        {
            survivor.ready(loc);
        }
    }

    public BoundingBox getBox()
    {
        return box;
    }

    public void addBullet(Bullet bullet)
    {
        this.bullets.add(bullet);
        bullet.spawnTo(Bukkit.getOnlinePlayers());
    }

    public Collection<BarragePlayer> getOnlinePlayers()
    {
        return onlinePlayers.values();
    }

    public Collection<Avoider> getSurvivors()
    {
        return survivors;
    }

    public void onJoin(Player player)
    {
        BarragePlayer barragePlayer = this.players.get(player.getUniqueId());

        if (barragePlayer != null)
        {
            barragePlayer.setPlayer(player);
            onlinePlayers.put(player, barragePlayer);
        }

        Collection<Player> c = Collections.singleton(player);

        for (Bullet bullet : bullets)
        {
            bullet.spawnTo(c);
        }
    }

    public void onQuit(Player player)
    {
        BarragePlayer barragePlayer = onlinePlayers.remove(player);

        if (barragePlayer != null)
        {
            barragePlayer.setPlayer(null);
        }
    }

    public void unregister()
    {
        HandlerList.unregisterAll(this.listener);
        task.cancel();

        for (Bullet bullet : bullets)
        {
            bullet.destroy();
        }
    }

    public BarragePlayer getBarragePlayer(Player player)
    {
        return this.onlinePlayers.get(player);
    }

    public List<Bullet> getBullets()
    {
        return bullets;
    }

    public void removeSurvivor(Avoider avoider)
    {
        this.survivors.remove(avoider);
    }

    public void stop()
    {
        plugin.stopProcess();
    }
}
