package org.mortbay.ijetty.util;


import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 *
 */
public class Constant
{

    /** 缓冲字节长度=1024*4B */
    public static final int BUFFER_LENGTH = 4096;

    public static final String MIME_PLAINTEXT = "text/plain";
    public static final String MIME_HTML = "text/html";
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    public static final String MIME_XML = "text/xml";

    public static final String ENCODING = "UTF-8";


    public static Hashtable theMimeTypes = new Hashtable();
    static
    {
        StringTokenizer st = new StringTokenizer(
                "css		text/css "+
                        "js			text/javascript "+
                        "htm		text/html "+
                        "html		text/html "+
                        "txt		text/plain "+
                        "asc		text/plain "+
                        "gif		image/gif "+
                        "jpg		image/jpeg "+
                        "jpeg		image/jpeg "+
                        "png		image/png "+
                        "mp3		audio/mpeg "+
                        "m3u		audio/mpeg-url " +
                        "pdf		application/pdf "+
                        "doc		application/msword "+
                        "ogg		application/x-ogg "+
                        "zip		application/octet-stream "+
                        "exe		application/octet-stream "+
                        "class		application/octet-stream " );
        while ( st.hasMoreTokens())
            theMimeTypes.put( st.nextToken(), st.nextToken());
    }

    public static interface MSG
    {
        public static final int GET_NETWORK_ERROR = -1;
        public static final int GET_NETWORK_OK = 0;
    }

    public static interface Config
    {
        public static final int JETTYPORT=8080;
        public static final int VIEWPORT = 5000;
        public static final int UPLOADPORT=8888;
        public static final int DOWNPOER=8081;
        //public static final String Web_Root = "/sdcard/jetty/webapps/console";
        public static final String Web_Root = "/storage/sdcard1/jetty/webapps/console";

    }

    public static interface Http
    {
        public static final String BROWSE = "*";
    }
}
