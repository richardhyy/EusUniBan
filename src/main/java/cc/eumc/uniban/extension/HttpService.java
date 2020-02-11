package cc.eumc.uniban.extension;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;

public class HttpService {
    public String path;
    public HttpHandler handler;
    public HttpContext context;

    /**
     * Init
     * @param path Starts with "/", eg. "/example"
     * @param handler
     */
    public HttpService(String path, HttpHandler handler) {
        this.path = path;
        this.handler = handler;
    }

    /**
     * It will be called once HttpContext was created by UniBan
     * @param context
     */
    public void setContext(HttpContext context) {
        this.context = context;
    }
}
