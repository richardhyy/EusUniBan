package cc.eumc.uniban.config;

import java.io.File;

public class ThirdPartySupportConfig {
    public static boolean AdvancedBan;
    public static boolean BungeeAdminTool;
    public static boolean BungeeBan;
    public static boolean LiteBans;
    public static boolean VanillaList;

    public static File DataFolder;

    public ThirdPartySupportConfig(File dataFolder, boolean advancedBan, boolean bungeeAdminTool, boolean bungeeBan, boolean liteBans, boolean vanillaList) {
        DataFolder = dataFolder;

        AdvancedBan = advancedBan;
        BungeeAdminTool = bungeeAdminTool;
        BungeeBan = bungeeBan;
        LiteBans = liteBans;
        VanillaList = vanillaList;
    }
}
