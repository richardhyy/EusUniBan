package cc.eumc.uniban.config;

import cc.eumc.uniban.UniBanBungeePlugin;
import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BungeeConfig extends PluginConfig {
    UniBanBungeePlugin plugin;
    public BungeeConfig(UniBanBungeePlugin instance) {
        super();

        // T√ODO Support for bungeecord
        // Fix: Broadcast disabled wrongly on BungeeCord
        this.plugin = instance;

        new ThirdPartySupportConfig(plugin.getDataFolder(),
                instance.getProxy().getPluginManager().getPlugin("AdvancedBan")!=null,
                instance.getProxy().getPluginManager().getPlugin("BungeeAdminTool")!=null,
                instance.getProxy().getPluginManager().getPlugin("BungeeBan")!=null,
                instance.getProxy().getPluginManager().getPlugin("LiteBans")!=null,
                new File(plugin.getDataFolder(), "banned-players.json").exists()
        );

    }


    @Override
    public void saveConfig() {
        UniBanBungeePlugin.getInstance().saveConfig();
    }

    @Override
    public boolean configGetBoolean(String path, Boolean def) {
        return getConfig().getBoolean(path, def);
    }

    @Override
    public String configGetString(String path, String def) {
        return getConfig().getString(path, def);
    }

    @Override
    public Double configGetDouble(String path, Double def) {
        return getConfig().getDouble(path, def);
    }

    @Override
    public int configGetInt(String path, int def) {
        return getConfig().getInt(path, def);
    }

    public Configuration getConfig() {
        return UniBanBungeePlugin.getInstance().getConfig();
    }

    @Override
    public List<String> configGetStringList(String path) {
        return getConfig().getStringList(path);
    }

    @Override
    public boolean configIsSection(String path) {
        return getConfig().getSection(path)!=null;
    }

    @Override
    public Set<String> getConfigurationSectionKeys(String path) {
        return new HashSet<>(getConfig().getSection(path).getKeys());
    }

    @Override
    public void configSet(String path, Object object) {
        getConfig().set(path, object);
    }
}
