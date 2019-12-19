package com.github.noonmaru.barrage.process;

import com.github.noonmaru.tap.entity.TapPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Nemo
 */
public abstract class BarragePlayer
{
    protected final BarrageProcess process;

    protected final UUID uniqueId;

    protected String name;

    protected Player player;

    protected TapPlayer tapPlayer;

    public BarragePlayer(BarrageProcess process, Player player)
    {
        this.process = process;
        this.uniqueId = player.getUniqueId();
        setPlayer(player);
    }

    public BarrageProcess getProcess()
    {
        return process;
    }

    public UUID getUniqueId()
    {
        return uniqueId;
    }

    public String getName()
    {
        return name;
    }

    public Player getPlayer()
    {
        return player;
    }

    public TapPlayer getTapPlayer()
    {
        return tapPlayer;
    }

    public void setPlayer(Player player)
    {
        if (player != null)
        {
            this.name = player.getName();
            this.player = player;
            this.tapPlayer = TapPlayer.wrapPlayer(player);
        }
        else
        {
            this.player = null;
            this.tapPlayer = null;
        }
    }

    public boolean isOnline()
    {
        return this.player != null;
    }

    public void onUpdate() {}
}
