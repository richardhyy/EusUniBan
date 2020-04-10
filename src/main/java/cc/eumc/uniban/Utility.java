package cc.eumc.uniban;

import cc.eumc.uniban.controller.CommandController;
import cc.eumc.uniban.util.Encryption;

public class Utility {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: <address:port>@<password> ... <address:port>@<password>");
            System.out.println("Example: example.com:60009@UniBan uniban.eumc.cc/public:443");
            return;
        }

        System.out.println("Subscription key(s) of your server(s) which contains your address and connection password:");

        for (String arg : args) {
            String[] split = arg.split("@");
            String address = "", password = "";

            if (split.length == 2) {
                address = split[0];
                password = split[1];
            }
            else if (split.length == 1){
                address = split[0];
            }
            if (!address.contains(":")) {
                address += ":60009";
                System.out.println("<!> " + address + " does not contain a valid port, use 60009 by default");
            }

            System.out.println(Encryption.encrypt(address + "@" + password, CommandController.SHARING_KEY));
        }
    }
}
