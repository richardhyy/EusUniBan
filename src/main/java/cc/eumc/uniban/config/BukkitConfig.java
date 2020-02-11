package cc.eumc.uniban.config;

import cc.eumc.uniban.UniBanBukkitPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

public class BukkitConfig extends PluginConfig {
    UniBanBukkitPlugin plugin;

    public BukkitConfig(UniBanBukkitPlugin instance) {
        super();
        new ThirdPartySupportConfig( instance.getServer().getPluginManager().isPluginEnabled("AdvancedBan"),
                instance.getServer().getPluginManager().isPluginEnabled("BungeeAdminTool"),
                instance.getServer().getPluginManager().isPluginEnabled("BungeeBan"),
                instance.getServer().getPluginManager().isPluginEnabled("LiteBans")
                );

        this.plugin = instance;
        if (BukkitConfig.ConfigVersion != BukkitConfig.PluginConfigVersion) {
            instance.getLogger().warning("Your configuration version is " + BukkitConfig.ConfigVersion + " which may not be well supported by the plugin. It is suggested that you backup and delete it, then reload UniBan.");
        }
    }

    @Override
    public void saveConfig() {
        UniBanBukkitPlugin.getInstance().saveConfig();
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

    public FileConfiguration getConfig() {
        return UniBanBukkitPlugin.getInstance().getConfig();
    }

    @Override
    public List<String> configGetStringList(String path) {
        return getConfig().getStringList(path);
    }

    @Override
    public boolean configIsSection(String path) {
        return getConfig().isConfigurationSection(path);
    }

    @Override
    public Set<String> getConfigurationSectionKeys(String path) {
        return getConfig().getConfigurationSection(path).getKeys(false);
    }

    @Override
    public void configSet(String path, Object object) {
        getConfig().set(path, object);
    }

}
