package cc.eumc.uniban.config;

public class ServerEntry {
    public String host;
    public int port;
    public int lastBannedAmount;

    public ServerEntry(String address) {
        int _port;
        if (address.contains(":")) { // Bug: Check if input is a IPv6 address
            host = address.substring(0, address.lastIndexOf(":")); // Fix wrong host address
            _port = Integer.parseInt(address.substring(address.lastIndexOf(":") + 1, address.length()));
        }
        else {
            host = address;
            _port = -1;
        }

        this.host = host;
        this.port = _port==0?60009:_port;
        this.lastBannedAmount = -1;
    }

    public ServerEntry(String host, int port) {
        this.host = host;
        this.port = port==0?60009:port;
        this.lastBannedAmount = -1;
    }

    public String getAddress() {
        return host+":"+port;
    }
}
