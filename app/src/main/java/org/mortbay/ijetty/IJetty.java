//========================================================================
//$Id: IJetty.java 474 2012-01-23 03:07:14Z janb.webtide $
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.ijetty;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.eclipse.jetty.util.IO;
import org.mortbay.ijetty.log.AndroidLog;
import org.mortbay.ijetty.util.FileExtraUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * IJetty
 * <p/>
 * Main Jetty activity. Can start other activities: + configure + download
 * <p/>
 * Can start/stop services: + IJettyService
 */
public class IJetty extends Activity {

    private static final String TAG = "Jetty";

    public static final String __START_ACTION = "org.mortbay.ijetty.start";
    public static final String __STOP_ACTION = "org.mortbay.ijetty.stop";

    public static final String __PORT = "org.mortbay.ijetty.port";
    public static final String __NIO = "org.mortbay.ijetty.nio";
    public static final String __SSL = "org.mortbay.ijetty.ssl";

    public static final String __CONSOLE_PWD = "org.mortbay.ijetty.console";
    public static final String __PORT_DEFAULT = "8080";
    public static final boolean __NIO_DEFAULT = true;
    public static final boolean __SSL_DEFAULT = false;

    public static final String __CONSOLE_PWD_DEFAULT = "admin";

    public static final String __WEBAPP_DIR = "webapps";
    public static final String __ETC_DIR = "etc";
    public static final String __CONTEXTS_DIR = "contexts";

    public static final String WEBAPPS = "webapps";
    public static final String CONSOLE = "console";
    public static final String JETTY = "jetty";

    public static final int CONFIRM_DIALOG_ID = 1;
    public static final int ERROR_DIALOG_ID = 2;
    public static final int PROGRESS_DIALOG_ID = 3;
    public static final int FINISH_DIALOG_ID = 4;



    public static final String __TMP_DIR = "tmp";
    public static final String __WORK_DIR = "work";
    public static final int __SETUP_PROGRESS_DIALOG = 0;
    public static final int __SETUP_DONE = 2;
    public static final int __SETUP_RUNNING = 1;
    public static final int __SETUP_NOTDONE = 0;

    public static final File __JETTY_DIR;
    /**
     * 开启Jetty服务
     */
    private Button startButton;
    /**
     * 关闭Jetty服务
     */
    private Button stopButton;
    /**
     * 安装WebApps
     */
    private Button configButton;
    /**
     * 访问WebApps
     */
    private Button view_webApps;
    private ProgressDialog progressDialog;
    private Thread progressThread;
    private Handler handler;


    private WifiManager wifiManager;


    static {
        __JETTY_DIR = new File(FileExtraUtils.getSDPath(),
                "jetty");
        Log.e("TAG","__JETTY_DIR:"+__JETTY_DIR);
        // Ensure parsing is not validating - does not work with android
        System.setProperty("org.eclipse.jetty.xml.XmlParser.Validating",
                "false");

        // Bridge Jetty logging to Android logging
        System.setProperty("org.eclipse.jetty.util.log.class",
                "org.mortbay.ijetty.AndroidLog");
        org.eclipse.jetty.util.log.Log.setLog(new AndroidLog());
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.jetty_controller);
        startButton = (Button) findViewById(R.id.start);
        stopButton = (Button) findViewById(R.id.stop);
        configButton = (Button) findViewById(R.id.config);
        view_webApps = (Button) findViewById(R.id.view_webApps);

        IntentFilter filter = new IntentFilter();
        filter.addAction(__START_ACTION);
        filter.addAction(__STOP_ACTION);
        filter.addCategory("default");

        // Watch for button clicks.
        startButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(IJetty.this, IJettyService.class);
                intent.putExtra(__PORT, __PORT_DEFAULT);
                intent.putExtra(__NIO, __NIO_DEFAULT);
                intent.putExtra(__SSL, __SSL_DEFAULT);
                intent.putExtra(__CONSOLE_PWD, __CONSOLE_PWD_DEFAULT);
                startService(intent);

            }
        });
        /**
         * 停止Jetty服务
         */
        stopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(IJetty.this, IJettyService.class));
            }
        });
        /**
         * 安装webapp
         */
        configButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(IJetty.this, InstallerActivity.class);
                startActivity(intent);
            }
        });
        /**
         * 访问WebApps
         */
        view_webApps.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IJetty.this, APIActivity.class);
                startActivity(intent);

            }
        });
        //获取wifi管理服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //setWifiApEnabled(true);
    }



    private void preWebAppsInstall(){
        File webapp = getWebApp();
        if (webapp == null)
            install();
    }

    /**
     * @return the File of the existing unpacked webapp or null
     */
    public File getWebApp() {
        File jettyDir = getJettyInstallDir();
        if (jettyDir == null)
            return null;

        File webappsDir = new File(jettyDir, WEBAPPS);
        if (!webappsDir.exists())
            return null;

        File webapp = new File(webappsDir, CONSOLE);
        if (!webapp.exists())
            return null;

        return webapp;
    }

    /**
     * Check to see if jetty has been installed.
     *
     * @return File of jetty install dir or null if not installed
     */
    public File getJettyInstallDir() {
        File jettyDir = new File(FileExtraUtils.getSDPath(), JETTY);
        if (!jettyDir.exists())
            return null;
        return jettyDir;
    }

    /**
     * Begin the installation
     */
    public void install() {
        Log.e("TAG","install");
        showDialog(PROGRESS_DIALOG_ID);
        InstallerThread thread = new InstallerThread(new Handler() {
            public void handleMessage(Message msg) {
                int total = msg.getData().getInt("prog");
                progressDialog.setProgress(total);
                if (total >= 100) {
                    progressDialog.dismiss();
                }
            }

            ;
        });
        thread.start();
    }


    /**
     * InstallerThread
     * <p/>
     * Perform the installation.
     */
    class InstallerThread extends Thread {
        private Handler handler;
        private boolean cleanInstall;

        public InstallerThread(Handler h) {
            handler = h;
        }

        public void sendProgressUpdate(int prog) {
            Message msg = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("prog", prog);
            msg.setData(b);
            handler.sendMessage(msg);
        }

        public void run() {
            sendProgressUpdate(50);

            try {
                extract(getResources().openRawResource(R.raw.hello));
                sendProgressUpdate(100);
            } catch (Exception e) {
                sendProgressUpdate(100);
                return;
            }
        }
    }

    /**
     * Extract the war.
     *
     * @param warStream
     * @throws IOException
     */
    public void extract(InputStream warStream){

        try {
            Log.e(JETTY, "1");
            if (warStream == null){
                Log.e(JETTY,"No war file found");
            }
            File jettyDir = getJettyInstallDir();
            if (jettyDir == null) {
                Log.e(JETTY, getString(R.string.jettyNotInstalled));
            }

            File webappsDir = new File(jettyDir, "webapps");
            if (!webappsDir.exists()) {
                Log.e(JETTY, getString(R.string.jettyNotInstalled));
            }
            File webapp = new File(webappsDir, "console");
            JarInputStream jin = new JarInputStream(warStream);
            JarEntry entry;
            while ((entry = jin.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                File file = new File(webapp, entryName);
                if (entry.isDirectory()) {
                    // Make directory
                    if (!file.exists())
                        file.mkdirs();
                } else {
                    // make directory (some jars don't list dirs)
                    File dir = new File(file.getParent());
                    if (!dir.exists())
                        dir.mkdirs();

                    // Make file
                    FileOutputStream fout = null;
                    try {
                        fout = new FileOutputStream(file);
                        IO.copy(jin, fout);
                    } finally {
                        IO.close(fout);
                    }

                    // touch the file.
                    if (entry.getTime() >= 0)
                        file.setLastModified(entry.getTime());
                }
            }
            Log.e(JETTY, "9");
            IO.close(jin);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(JETTY,e.getMessage().toString());
        }
    }


    /**
     * ProgressThread
     * <p/>
     * Handles finishing install tasks for Jetty.
     */
    class ProgressThread extends Thread {
        private Handler _handler;

        public ProgressThread(Handler h) {
            _handler = h;
        }

        public void sendProgressUpdate(int prog) {
            Message msg = _handler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("prog", prog);
            msg.setData(b);
            _handler.sendMessage(msg);
        }

        public void run() {
            boolean updateNeeded = isUpdateNeeded();

            // create the jetty dir structure
            File jettyDir = __JETTY_DIR;
            if (!jettyDir.exists()) {
                boolean made = jettyDir.mkdirs();
                Log.i(TAG, "Made " + __JETTY_DIR + ": " + made);
            }

            sendProgressUpdate(10);

            // Do not make a work directory to preserve unpacked
            // webapps - this seems to clash with Android when
            // out-of-date webapps are deleted and then re-unpacked
            // on a jetty restart: Android remembers where the dex
            // file of the old webapp was installed, but it's now
            // been replaced by a new file of the same name. Strangely,
            // this does not seem to affect webapps unpacked to tmp?
            // Original versions of i-jetty created a work directory. So
            // we will delete it here if found to ensure webapps can be
            // updated successfully.
            File workDir = new File(jettyDir, __WORK_DIR);
            if (workDir.exists()) {
                Installer.delete(workDir);
                Log.i(TAG, "removed work dir");
            }

            // make jetty/tmp
            File tmpDir = new File(jettyDir, __TMP_DIR);
            if (!tmpDir.exists()) {
                boolean made = tmpDir.mkdirs();
                Log.i(TAG, "Made " + tmpDir + ": " + made);
            } else {
                Log.i(TAG, tmpDir + " exists");
            }

            // make jetty/webapps
            File webappsDir = new File(jettyDir, __WEBAPP_DIR);
            if (!webappsDir.exists()) {
                boolean made = webappsDir.mkdirs();
                Log.i(TAG, "Made " + webappsDir + ": " + made);
            } else {
                Log.i(TAG, webappsDir + " exists");
            }

            // make jetty/etc
            File etcDir = new File(jettyDir, __ETC_DIR);
            if (!etcDir.exists()) {
                boolean made = etcDir.mkdirs();
                Log.i(TAG, "Made " + etcDir + ": " + made);
            } else {
                Log.i(TAG, etcDir + " exists");
            }
            sendProgressUpdate(30);

            File webdefaults = new File(etcDir, "webdefault.xml");
            if (!webdefaults.exists() || updateNeeded) {
                // get the webdefaults.xml file out of resources
                try {
                    InputStream is = getResources().openRawResource(
                            R.raw.webdefault);
                    OutputStream os = new FileOutputStream(webdefaults);
                    IO.copy(is, os);
                    Log.i(TAG, "Loaded webdefault.xml");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading webdefault.xml", e);
                }
            }
            sendProgressUpdate(40);

            File realm = new File(etcDir, "realm.properties");
            if (!realm.exists() || updateNeeded) {
                try {
                    // get the realm.properties file out resources
                    InputStream is = getResources().openRawResource(
                            R.raw.realm_properties);
                    OutputStream os = new FileOutputStream(realm);
                    IO.copy(is, os);
                    Log.i(TAG, "Loaded realm.properties");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading realm.properties", e);
                }
            }
            sendProgressUpdate(50);

            File keystore = new File(etcDir, "keystore");
            if (!keystore.exists() || updateNeeded) {
                try {
                    // get the keystore out of resources
                    InputStream is = getResources().openRawResource(
                            R.raw.keystore);
                    OutputStream os = new FileOutputStream(keystore);
                    IO.copy(is, os);
                    Log.i(TAG, "Loaded keystore");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading keystore", e);
                }
            }
            sendProgressUpdate(60);

            // make jetty/contexts
            File contextsDir = new File(jettyDir, __CONTEXTS_DIR);
            if (!contextsDir.exists()) {
                boolean made = contextsDir.mkdirs();
                Log.i(TAG, "Made " + contextsDir + ": " + made);
            } else {
                Log.i(TAG, contextsDir + " exists");
            }
            sendProgressUpdate(70);

            try {
                PackageInfo pi = getPackageManager().getPackageInfo(
                        getPackageName(), 0);
                if (pi != null) {
                    setStoredJettyVersion(pi.versionCode);
                }
            } catch (Exception e) {
                Log.w(TAG, "Unable to get PackageInfo for i-jetty");
            }

            // if there was a .update file indicating an update was needed,
            // remove it now we've updated
            File update = new File(__JETTY_DIR, ".update");
            if (update.exists())
                update.delete();

            sendProgressUpdate(100);
        }
    }

    ;


    public IJetty() {
        super();

        handler = new Handler() {
            public void handleMessage(Message msg) {
                int total = msg.getData().getInt("prog");
                progressDialog.setProgress(total);
                if (total >= 100) {
                    dismissDialog(__SETUP_PROGRESS_DIALOG);
                }
            }

        };
    }


    protected int getStoredJettyVersion() {
        File jettyDir = __JETTY_DIR;
        if (!jettyDir.exists()) {
            return -1;
        }
        File versionFile = new File(jettyDir, "version.code");
        if (!versionFile.exists()) {
            return -1;
        }
        int val = -1;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(versionFile));
            val = ois.readInt();
            return val;
        } catch (Exception e) {
            Log.e(TAG, "Problem reading version.code", e);
            return -1;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (Exception e) {
                    Log.d(TAG, "Error closing version.code input stream", e);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public static void show(Context context) {
        final Intent intent = new Intent(context, IJetty.class);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        if (!SdCardUnavailableActivity.isExternalStorageAvailable()) {
            SdCardUnavailableActivity.show(this);
        } else {
            // work out if we need to do the installation finish step
            // or not. We do it iff:
            // - there is no previous jetty version on disk
            // - the previous version does not match the current version
            // - we're not already doing the update
            if (isUpdateNeeded()) {
                setupJetty();
            }
        }

        if (IJettyService.isRunning()) {
            startButton.setEnabled(false);
            configButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            startButton.setEnabled(true);
            configButton.setEnabled(true);
            stopButton.setEnabled(false);
            Intent intent = new Intent(IJetty.this, IJettyService.class);
            intent.putExtra(__PORT, __PORT_DEFAULT);
            intent.putExtra(__NIO, __NIO_DEFAULT);
            intent.putExtra(__SSL, __SSL_DEFAULT);
            intent.putExtra(__CONSOLE_PWD, __CONSOLE_PWD_DEFAULT);
            startService(intent);
            preWebAppsInstall();
        }
        super.onResume();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case __SETUP_PROGRESS_DIALOG: {
                progressDialog = new ProgressDialog(IJetty.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("Finishing initial install ...");

                return progressDialog;
            }
            default:
                return null;
        }
    }

    protected void setStoredJettyVersion(int version) {
        File jettyDir = __JETTY_DIR;
        if (!jettyDir.exists()) {
            return;
        }
        File versionFile = new File(jettyDir, "version.code");
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fos = new FileOutputStream(versionFile);
            oos = new ObjectOutputStream(fos);
            oos.writeInt(version);
            oos.flush();
        } catch (Exception e) {
            Log.e(TAG, "Problem writing jetty version", e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (Exception e) {
                    Log.d(TAG, "Error closing version.code output stream", e);
                }
            }
        }
    }

    /**
     * We need to an update iff we don't know the current jetty version or it is
     * different to the last version that was installed.
     *
     * @return
     */
    public boolean isUpdateNeeded() {
        // if no previous version file, assume update is required
        int storedVersion = getStoredJettyVersion();
        if (storedVersion <= 0)
            return true;

        try {
            // if different previous version, update is required
            PackageInfo pi = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            if (pi == null)
                return true;
            if (pi.versionCode != storedVersion)
                return true;

            // if /sdcard/jetty/.update file exists, then update is required
            File alwaysUpdate = new File(__JETTY_DIR, ".update");
            if (alwaysUpdate.exists()) {
                Log.i(TAG, "Always Update tag found " + alwaysUpdate);
                return true;
            }
        } catch (Exception e) {
            // if any of these tests go wrong, best to assume update is true?
            return true;
        }

        return false;
    }

    public void setupJetty() {
        showDialog(__SETUP_PROGRESS_DIALOG);
        progressThread = new ProgressThread(handler);
        progressThread.start();
    }



    // wifi热点开关
    public boolean setWifiApEnabled(boolean enabled) {
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = "kristain";
            //配置热点的密码
            apConfig.preSharedKey="12122112";
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }


}
