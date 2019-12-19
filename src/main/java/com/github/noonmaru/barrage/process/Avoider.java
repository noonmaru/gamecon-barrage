package com.github.noonmaru.barrage.process;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

/**
 * @author Nemo
 */
public class Avoider extends BarragePlayer
{
    private final Score score;

    private boolean dead;

    public Avoider(BarrageProcess process, Player player, Score score)
    {
        super(process, player);
        this.score = score;
    }

    @Override
    public void onUpdate()
    {
        if (dead)
            return;

        score.setScore(score.getScore() + 1);
    }

    public void die()
    {
        dead = true;
        player.setGameMode(GameMode.SPECTATOR);
        process.removeSurvivor(this);
    }

    public boolean isAlive()
    {
        return !dead;
    }

    public void ready(Location loc)
    {
        player.teleport(loc);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.getInventory().clear();
    }
}
