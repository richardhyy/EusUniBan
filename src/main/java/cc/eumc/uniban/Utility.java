package cc.eumc.uniban;

import cc.eumc.uniban.controller.CommandController;
import cc.eumc.uniban.util.Encryption;

public class Utility {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: <address:port> ... <address:port>");
            System.out.println("Example: example.com:60009 uniban.eumc.cc/public:443");
            return;
        }

        System.out.println("Subscription key(s) of your server(s) which contains your address and connection password:");

        for (String arg : args) {
            String address = arg;
            if (!address.contains(":")) {
                address += ":60009";
                System.out.println("<!> " + address + " does not contain a valid port, use 60009 by default");
            }
            System.out.println(Encryption.encrypt(address + "@", CommandController.SHARING_KEY));
        }
    }
}
