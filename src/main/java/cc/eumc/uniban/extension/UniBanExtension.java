package cc.eumc.uniban.extension;

import cc.eumc.uniban.UniBanBukkitPlugin;
import cc.eumc.uniban.UniBanBungeePlugin;
import cc.eumc.uniban.controller.UniBanController;
import com.sun.istack.internal.Nullable;

public abstract class UniBanExtension {
    public abstract void onExtensionLoad();
    public abstract void onExtensionUnload();

    public abstract @Nullable
    HttpService getHttpService();

    public abstract String getName();
    public abstract String getAuthor();
    public abstract String getVersion();

    public static UniBanController getUniBanController() {
        if (UniBanBukkitPlugin.getInstance() != null) {
            return UniBanBukkitPlugin.getInstance().getController();
        }
        else {
            return UniBanBungeePlugin.getInstance().getController();
        }
    }

    public void register() {
        getUniBanController().registerExtension(this);
    }
}
