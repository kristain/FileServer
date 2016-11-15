package org.mortbay.ijetty;

/**
 * Created by kristain on 16/3/9.
 */
public class GlobalApplication extends MApplication {

    private static GlobalApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public GlobalApplication() {
        app = this;
    }

    public static synchronized GlobalApplication getInstance() {
        if (app == null) {
            app = new GlobalApplication();
        }
        return app;
    }

    /**
     * 退出APP时手动调用
     */
    @Override
    public void exit() {
        try {
            //关闭所有Activity
            //退出进程
            System.exit(0);
        } catch (Exception e) {
        }
    }

}