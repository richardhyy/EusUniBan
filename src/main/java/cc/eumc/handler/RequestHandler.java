package cc.eumc.handler;

import cc.eumc.UniBanPlugin;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class RequestHandler implements HttpHandler {
    UniBanPlugin plugin;

    public RequestHandler(UniBanPlugin instance) {
        this.plugin = instance;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!plugin.getAccessController().canAccess(t.getRemoteAddress().getHostName())) {
            // if host was blocked
            t.close();
            return;
        }
        plugin.getLogger().info("Ban-list request from: " + t.getRemoteAddress().getHostName());
        String response = plugin.getBanListJson();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
