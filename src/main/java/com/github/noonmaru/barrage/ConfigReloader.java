package com.github.noonmaru.barrage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author Nemo
 */
public class ConfigReloader implements Runnable
{
    private final File file;

    private long lastModified;

    private final Consumer<ConfigurationSection> applier;

    private final Consumer<String> logger;

    public ConfigReloader(File file, Consumer<ConfigurationSection> applier, Consumer<String> logger)
    {
        this.file = file;
        this.lastModified = file.lastModified();
        this.applier = applier;
        this.logger = logger;
    }

    @Override
    public void run()
    {
        long last = file.lastModified();

        if (this.lastModified != last)
        {
            this.lastModified = last;

            if (file.exists())
            {
                applier.accept(YamlConfiguration.loadConfiguration(file));
                logger.accept("Reload config");
            }
        }
    }
}
