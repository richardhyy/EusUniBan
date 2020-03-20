package cc.eumc.uniban.handler;

import cc.eumc.uniban.config.PluginConfig;
import cc.eumc.uniban.controller.AccessController;
import cc.eumc.uniban.controller.UniBanController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class IDRequestHandler implements HttpHandler {
    UniBanController controller;
    AccessController accessController = new AccessController();

    public IDRequestHandler(UniBanController instance) {
        this.controller = instance;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!accessController.canAccess(t.getRemoteAddress().getHostName())) {
            // if host was blocked
            t.close();
            return;
        }
        controller.sendInfo("ID request from: " + t.getRemoteAddress().getHostName());
        String response = PluginConfig.NodeID;
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
