package org.mortbay.ijetty.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by kristain on 16/4/25.
 */
public class MD5Util {

    protected static char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    protected static MessageDigest messageDigest =null;
    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        }catch (NoSuchAlgorithmException e){

        }
    }



    /**
     * 生成文件的Md5校验值
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(File file) throws IOException{
        InputStream fis;
        fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int numRead = 0;
        while((numRead = fis.read(buffer))>0){
            messageDigest.update(buffer,0,numRead);
        }
        fis.close();
        return bufferToHex(messageDigest.digest());
    }


    private static String bufferToHex(byte bytes[]){
      return bufferToHex(bytes,0,bytes.length);
    }

    private static String bufferToHex(byte bytes[],int m,int n){
        StringBuffer stringBuffer = new StringBuffer(2*n);
        int k = m+n;
        for (int i=m;i<k;i++){
            appendHexPair(bytes[i],stringBuffer);
        }
        return stringBuffer.toString();
    }


    private static void appendHexPair(byte bt,StringBuffer stringBuffer){
        char c0 = hexDigits[(bt & 0xf0)>>4];
        char c1 = hexDigits[bt & 0xf];
        stringBuffer.append(c0);
        stringBuffer.append(c1);
    }


}
