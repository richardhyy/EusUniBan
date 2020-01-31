package cc.eumc.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateCheckTask implements Runnable {

    String version;
    int resourceId;

    public UpdateCheckTask(String version, int resourceId) {
        this.version = version;
        this.resourceId = resourceId;
    }

    @Override
    public void run() {
        getVersion(version -> {
            if (version.equalsIgnoreCase("Invalid resource")) {
                System.out.println("[UniBan] It looks like you are using an unsupported version of UniBan. Please manually look for update.");
            }
            if (version.equalsIgnoreCase(this.version)) {
                System.out.println("[UniBan] You are up-to-date.");
            } else {
                System.out.println("[UniBan] There is a newer version (" + version + ") available at §n https://www.spigotmc.org/resources/uniban-a-decentralized-ban-list-sharing-subscribing-plugin.74747/");
            }
        });
    }

    public void getVersion(final Consumer<String> consumer) {
        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                consumer.accept(scanner.next());
            }
        } catch (IOException e) {
            System.out.println("[UniBan] Error occurred when checking update.");
        }
    }
}
