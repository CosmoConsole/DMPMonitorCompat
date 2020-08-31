
package email.com.gmail.cosmoconsole.bukkit.dmp.monitor;

import org.bukkit.plugin.java.*;

import email.com.gmail.cosmoconsole.bukkit.deathmsg.DMPReloadEvent;
import email.com.gmail.cosmoconsole.bukkit.deathmsg.DeathMessagesPrime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Main extends JavaPlugin implements Listener
{
    public final int CONFIG_VERSION = 1;
    boolean debug;
    boolean restrictive;
    DeathMessagesPrime dmp;
    FileConfiguration config;
    Set<String> plugins;
    private static ThreadLocal<Boolean> antiNest = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };
    
    public Main() {
        this.debug = false;
        this.restrictive = true;
        this.config = null;
        this.plugins = new HashSet<>();
    }
    
    public void onEnable() {
        dmp = (DeathMessagesPrime) getServer().getPluginManager().getPlugin("DeathMessagesPrime");
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.loadConfig();
    }

    @EventHandler
    public void reloadConfig(DMPReloadEvent e) {
        this.loadConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void passToMonitor(PlayerDeathEvent pde) {
        if (antiNest.get()) {
            return;
        }
        
        antiNest.set(true);
        String dmsg = dmp.getDeathMessage(pde);
        if (dmsg == null || dmsg.isEmpty()) {
            antiNest.set(false);
            return;
        }
        
        // send mock message
        PlayerDeathEvent mock = new PlayerDeathEvent(pde.getEntity(), pde.getDrops(), pde.getDroppedExp(), -1, -1, -1, dmsg);
        for (RegisteredListener rl: pde.getHandlers().getRegisteredListeners()) {
            if (plugins.contains(rl.getPlugin().getName()) && rl.getPlugin() != this
                                            && (!this.restrictive 
                                            || rl.getPriority() == EventPriority.HIGHEST 
                                            || rl.getPriority() == EventPriority.MONITOR)) {
                try {
                    rl.callEvent(mock);
                } catch (EventException e) {
                    e.printStackTrace();
                }
            }
        }
        antiNest.set(false);
    }
    
    private void loadConfig() {
        this.config = this.getConfig();
        try {
            this.config.load(new File(this.getDataFolder(), "config.yml"));
            if (!this.config.contains("config-version")) {
                throw new Exception();
            }
            if (this.config.getInt("config-version") < CONFIG_VERSION) {
                throw new ConfigTooOldException();
            }
        }
        catch (FileNotFoundException e6) {
            this.getLogger().info("Extracting default config.");
            this.saveResource("config.yml", true);
            try {
                this.config.load(new File(this.getDataFolder(), "config.yml"));
            }
            catch (IOException | InvalidConfigurationException ex3) {
                ex3.printStackTrace();
                this.getLogger().severe("The JAR config is broken, disabling");
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
                this.setEnabled(false);
            }
        }
        catch (ConfigTooOldException e3) {
            this.getLogger().warning("!!! WARNING !!! Your configuration is old. There may be new features or some config behavior might have changed, so it is advised to regenerate your config when possible!");
        }
        catch (Exception e4) {
            e4.printStackTrace();
            this.getLogger().severe("Configuration is invalid. Re-extracting it.");
            final boolean success = !new File(this.getDataFolder(), "config.yml").isFile() || new File(this.getDataFolder(), "config.yml").renameTo(new File(this.getDataFolder(), "config.yml.broken" + new Date().getTime()));
            if (!success) {
                this.getLogger().severe("Cannot rename the broken config, disabling");
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
                this.setEnabled(false);
            }
            this.saveResource("config.yml", true);
            try {
                this.config.load(new File(this.getDataFolder(), "config.yml"));
            }
            catch (IOException | InvalidConfigurationException ex4) {
                ex4.printStackTrace();
                this.getLogger().severe("The JAR config is broken, disabling");
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
                this.setEnabled(false);
            }
        }
        this.debug = this.config.getBoolean("debug", false);
        this.restrictive = this.config.getBoolean("only-monitor-or-highest", true);
        this.plugins.clear();
        if (this.config.contains("plugins"))
            this.plugins.addAll(this.config.getStringList("plugins"));
    }
}

