package com.github.noonmaru.barrage;

import com.github.noonmaru.barrage.process.BarrageProcess;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.nbt.NBTCompound;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public final class BarragePlugin extends JavaPlugin
{
    private final Map<UUID, TapItemStack> bulletItems = new HashMap<>();

    private File bulletItemsDir;

    private BarrageProcess process;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        BarrageConfig.load(getConfig());

        getServer().getScheduler().runTaskTimer(this, new ConfigReloader(new File(getDataFolder(), "config.yml"), BarrageConfig::load, getLogger()::info), 0, 1);

        this.bulletItemsDir = new File(getDataFolder(), "bulletItems");
        bulletItemsDir.mkdirs();

        for (File file : Objects.requireNonNull(bulletItemsDir.listFiles((dir, name) -> name.endsWith(".dat"))))
        {
            String name = StringUtils.removeEnd(file.getName(), ".dat");
            UUID uuid = UUID.fromString(name);

            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file)))
            {
                NBTCompound compound = Tap.NBT.loadCompound(in);
                TapItemStack item = Tap.ITEM.loadItemStack(compound);

                bulletItems.put(uuid, item);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable()
    {
        stopProcess();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0)
            return false;

        String sub = args[0];

        if ("start".equalsIgnoreCase(sub))
        {
            if (process != null)
            {
                sender.sendMessage("게임이 진행중입니다.");
                return true;
            }

            if (args.length < 2)
            {
                sender.sendMessage("/" + label + " " + sub + " [Player...] 피할 사람");
                return true;
            }

            Set<Player> avoiders = new HashSet<>();

            for (int i = 1; i < args.length; i++)
            {
                String name = args[i];
                Player player = Bukkit.getPlayerExact(name);

                if (player == null)
                {
                    sender.sendMessage("Not found player: " + name);
                    return true;
                }

                avoiders.add(player);
            }

            startProcess(avoiders);
            sender.sendMessage("탄막 피하기 게임을 시작했습니다.");
        }
        else if ("stop".equalsIgnoreCase(sub))
        {
            stopProcess();
            sender.sendMessage("탄막 피하기 게임을 종료했습니다.");
        }

        return true;
    }

    public void startProcess(Set<Player> players)
    {
        if (process != null)
            return;

        process = new BarrageProcess(this, players);
    }

    public void stopProcess()
    {
        if (process == null)
            return;

        process.unregister();
        process = null;
    }

    public TapItemStack getBulletItem(Player player)
    {
        TapItemStack item =  bulletItems.get(player.getUniqueId());

        if (item == null)
            item = Tap.ITEM.newItemStack("stone", 1, 0);

        return item;
    }

    public void setBulletItem(UUID uniqueId, TapItemStack item)
    {
        bulletItems.put(uniqueId, item);

        bulletItemsDir.mkdirs();

        try
        {
            item.save().save(new File(bulletItemsDir, uniqueId.toString() + ".dat"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
