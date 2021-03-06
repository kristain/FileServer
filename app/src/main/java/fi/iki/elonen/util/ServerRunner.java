package fi.iki.elonen.util;

import java.io.IOException;

/**
 * 写文件服务
 */
public class ServerRunner {
    private static NanoHTTPD httpserver;
    private static boolean isRunning = false;

    /**
     * 启动http服务
     * @param port 服务端口号
     */
    public static void start(String webRoot, int port){
        httpserver = new SimpleHttpServer(webRoot, port);
        if(!isRunning){
            try {
                httpserver.start();
                isRunning = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭http服务
     */
    public static void stop(){
        if(null != httpserver){
            httpserver.stop();
            isRunning = false;
        }
    }
}
