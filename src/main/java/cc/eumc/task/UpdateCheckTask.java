package cc.eumc.task;

import cc.eumc.config.Message;

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
                System.out.println(Message.MessagePrefix + Message.InvalidSpigotResourceID);
            }
            if (version.equalsIgnoreCase(this.version)) {
                System.out.println(Message.MessagePrefix + Message.UpToDate);
            } else {
                System.out.println(Message.MessagePrefix + String.format(Message.NewVersionAvailable, version));
            }
        });
    }

    public void getVersion(final Consumer<String> consumer) {
        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                consumer.accept(scanner.next());
            }
        } catch (IOException e) {
            System.out.println(Message.MessagePrefix + Message.FailedCheckingUpdate);
        }
    }
}
