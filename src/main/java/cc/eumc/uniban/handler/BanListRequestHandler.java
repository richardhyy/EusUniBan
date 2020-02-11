package cc.eumc.uniban.handler;

import cc.eumc.uniban.controller.AccessController;
import cc.eumc.uniban.controller.UniBanController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class BanListRequestHandler implements HttpHandler {
    UniBanController controller;
    AccessController accessController = new AccessController();

    public BanListRequestHandler(UniBanController instance) {
        this.controller = instance;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!accessController.canAccess(t.getRemoteAddress().getHostName())) {
            // if host was blocked
            t.close();
            return;
        }
        controller.sendInfo("Ban-list request from: " + t.getRemoteAddress().getHostName());
        String response = controller.getBanListJson();
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
