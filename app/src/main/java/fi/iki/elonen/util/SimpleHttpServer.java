package fi.iki.elonen.util;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by kristain
 */
public class SimpleHttpServer extends NanoHTTPD {

    private final static String TAG="SimpleHttpServer";
    private final String webRoot;

    public SimpleHttpServer(String webRoot, int port){
        super(port);
        this.webRoot = webRoot;
    }

    @Override
    public Response serve(IHTTPSession session){
        Method method = session.getMethod();
        if(Method.POST.equals(method)){
            return new Response(doPost(session));
        }
        return defaultResponse(session);
    }

    /**
     * 返回文件上传的静态页面
     * @param session
     * @return
     */
    private Response defaultResponse(IHTTPSession session){
        return new Response(DefaultHtml.HTML_STRING);
    }

    /**
     * POST方法先进行文件存储
     * @param session
     */
    private String doPost(IHTTPSession session){
        JSONObject res = new JSONObject();
        try {
            Map<String, String> params = session.getParms();
            Iterator<String> a = params.keySet().iterator();
            while (a.hasNext()){
                String value = a.next();
               Log.e("TAG1",value+":"+params.get(value));
            }
            //String fileType="";
            //String saveDir="";
            Map<String, String> files = new HashMap<String, String>();
            session.parseBody(files);
            /*Iterator<String> e = params.keySet().iterator();
            while (e.hasNext()){
                String value = e.next();
                Log.e("TAG",value+":"+params.get(value));
                if("fileType".equals(value)){
                    fileType = params.get(value);
                }
                if("saveDir".equals(value)){
                    saveDir = params.get(value);
                }
            }
            if(!FileTypeEnum.isFileType(fileType)){
                res.put("error","1");
                res.put("message","fileType error");
                return res.toString();
            }
            if(!StringUtils.isEmpty(saveDir)){
                Log.d(TAG, "TAG saveDir 不为空:" + saveDir);
                final File file = new File(this.webRoot + "/"+FileTypeEnum.getMsgByCode(fileType) +"/"+saveDir);
                if (!file.exists()) {
                    Log.d(TAG, "TAG file is not exist");
                    FileExtraUtils.createDir(this.webRoot + "/" + FileTypeEnum.getMsgByCode(fileType) + "/" + saveDir);
                }else{
                    Log.d(TAG, "TAG file is  exist");
                }
            }
            Log.e(TAG, "上传文件path:" + this.webRoot + "/" + FileTypeEnum.getMsgByCode(fileType) + "/" + saveDir);*/

            /*for(Map.Entry<String, String> entry : files.entrySet()){
                String srcFilePath = entry.getValue();
                String destFilePath = this.webRoot + "/"+FileTypeEnum.getMsgByCode(fileType) +"/"+saveDir+"/"+URLDecoder.decode(params.get(entry.getKey()), "UTF-8");
                Log.e(TAG,"TAG srcFilePath:"+srcFilePath + "destFilePath:" + destFilePath);
                FileExtraUtils.copyFile(srcFilePath, destFilePath, false);
            }*/
            res.put("error","0");
            res.put("message","success");
            Log.e("TAG","return "+res.toString());
            return res.toString();
        } catch (IOException e) {
            e.printStackTrace();
            res.put("error", "1");
            res.put("message","failure"+e.getMessage());
            Log.e("TAG", "return " + res.toString());
            return res.toString();
        } catch (ResponseException e) {
            e.printStackTrace();
            res.put("error", "1");
            res.put("message","failure"+e.getMessage());
            Log.e("TAG", "return " + res.toString());
            return res.toString();
        }
    }
}
