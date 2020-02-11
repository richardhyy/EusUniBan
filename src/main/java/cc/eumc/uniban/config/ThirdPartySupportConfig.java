package cc.eumc.uniban.config;

public class ThirdPartySupportConfig {
    public static boolean AdvancedBan;
    public static boolean BungeeAdminTool;
    public static boolean BungeeBan;
    public static boolean LiteBans;

    public ThirdPartySupportConfig(boolean advancedBan, boolean bungeeAdminTool, boolean bungeeBan, boolean liteBans) {
        AdvancedBan = advancedBan;
        BungeeAdminTool = bungeeAdminTool;
        BungeeBan = bungeeBan;
        LiteBans = liteBans;
    }
}
