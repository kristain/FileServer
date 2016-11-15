package org.mortbay.ijetty.http;


import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.mortbay.ijetty.entity.ActionEnum;
import org.mortbay.ijetty.entity.FileTypeEnum;
import org.mortbay.ijetty.util.CommonUtil;
import org.mortbay.ijetty.util.Constant;
import org.mortbay.ijetty.util.FileExtraUtils;
import org.mortbay.ijetty.util.StringUtils;
import org.mortbay.ijetty.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 浏览文件列表处理器
 */
public class FileBrowseHandler implements HttpRequestHandler {
    private static final String TAG = FileBrowseHandler.class.getSimpleName();
    private String webRoot;

    public FileBrowseHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse,
                       HttpContext httpContext) throws HttpException, IOException {
        try {
            JSONObject retObject = new JSONObject();
            String target = URLDecoder.decode(httpRequest.getRequestLine().getUri(),
                    Constant.ENCODING);
            Log.d(TAG, "TAG http request url:" + target);
            if (target.contains("favicon.ico")) {
                return;
            }
            if (isFile(target)) {
                final File file = new File(webRoot.replace("/console","")  + target);
                Log.d(TAG, "TAG file url:" + webRoot.replace("/console","") + target);
                if (file != null && !file.exists()) {
                    retObject.put("error", "1");
                    retObject.put("message", "文件不存在");
                    HttpEntity entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                    Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
                    httpResponse.setEntity(entity);
                    return;
                }
                if (file.canRead()) {
                    String mime = null;
                    int dot = file.getCanonicalPath().lastIndexOf(".");
                    if (dot >= 0) {
                        mime = CommonUtil.setContentType(target);
                        if (TextUtils.isEmpty(mime))
                            mime = Constant.MIME_DEFAULT_BINARY;

                        httpResponse.setHeader("Content-Type", mime);
                        Header[] resheaders = httpResponse.getAllHeaders();
                        for (Header header : resheaders) {
                            Log.d(TAG, "TAG resheaders :" + header.getName() + ":" + header.getValue());//Range: bytes=0-1
                        }

                        HttpEntity entity = new EntityTemplate(new ContentProducer() {
                            @Override
                            public void writeTo(OutputStream outStream) throws IOException {
                                write(file, outStream);
                            }
                        });
                        httpResponse.setEntity(entity);
                        return;
                    }
                }
                //没有权限读取文件
                retObject.put("error", "0");
                retObject.put("message", "文件存在");
                HttpEntity entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
                httpResponse.setEntity(entity);
                return;
            }
            HttpEntity httpEntity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();
            String requestParamter = EntityUtils.toString(httpEntity);
            Log.d(TAG, "TAG http request paramter:" + requestParamter);

            HttpEntity entity = new StringEntity("", Constant.ENCODING);
            /**************************** 请求参数校验开始**************************************/

            if (StringUtils.isEmpty(requestParamter)) {
                //缺少请求参数
                httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                retObject.put("error", "1");
                retObject.put("message", "缺少请求参数[action]");
                entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
                httpResponse.setEntity(entity);
                return;
            }
            Map<String, String> mapRequest = CRequest.URLRequest("?" + requestParamter);
            if (!ActionEnum.isAction(mapRequest.get("action"))) {
                //缺少请求参数
                httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                retObject.put("error", "1");
                retObject.put("message", "缺少请求参数[action]");
                entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
                httpResponse.setEntity(entity);
                return;
            }
            if (ActionEnum.DELFILE.getCode().equals(mapRequest.get("action"))) {
                if (!FileTypeEnum.isFileType(mapRequest.get("type"))) {
                    retObject.put("message", "请求参数type[" + mapRequest.get("type") + "]错误");
                }
                if (StringUtils.isEmpty(mapRequest.get("name"))) {
                    retObject.put("message", "请求参数name[" + mapRequest.get("name") + "]错误");
                }
                httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                retObject.put("error", "1");
                entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
                httpResponse.setEntity(entity);
            }
            /******************************请求参数校验结束************************************/


            if (target == "" || !target.contains(webRoot)) {
                target = webRoot;
            }
            if (ActionEnum.FILELIST.getCode().equals(mapRequest.get("action"))) {
                target = webRoot + "/Files";
            } else if (ActionEnum.VIDEOLIST.getCode().equals(mapRequest.get("action"))) {
                target = webRoot + "/Videos";
            } else if (ActionEnum.IMAGELIST.getCode().equals(mapRequest.get("action"))) {
                target = webRoot + "/Pictures";
            } else if (ActionEnum.MUSICLIST.getCode().equals(mapRequest.get("action"))) {
                target = webRoot + "/Musics";
            } else if (ActionEnum.FILESUM.getCode().equals(mapRequest.get("action"))) {
                target = webRoot;
            } else if (ActionEnum.DELFILE.getCode().equals(mapRequest.get("action"))) {
                //删除文件
                if (FileTypeEnum.FILE.getCode().equals(mapRequest.get("type"))) {
                    target = webRoot + "/Files/" + URLDecoder.decode(mapRequest.get("name")).replace("%2f", "/");
                } else if (FileTypeEnum.IMAGE.getCode().equals(mapRequest.get("type"))) {
                    target = webRoot + "/Pictures/" + URLDecoder.decode(mapRequest.get("name")).replace("%2f", "/");
                } else if (FileTypeEnum.MUSIC.getCode().equals(mapRequest.get("type"))) {
                    target = webRoot + "/Musics/" +URLDecoder.decode(mapRequest.get("name")).replace("%2f", "/");
                } else if (FileTypeEnum.VIDEO.getCode().equals(mapRequest.get("type"))) {
                    target = webRoot + "/Videos/" + URLDecoder.decode(mapRequest.get("name")).replace("%2f", "/");
                }
                Log.e("TAG", "删除文件路径:" + target);
            }

            final File file = new File(target);
            String contentType = "text/html;charset=" + Constant.ENCODING;
            //删除文件
            if (ActionEnum.DELFILE.getCode().equals(mapRequest.get("action"))) {
                Log.e(TAG, "TAG 删除文件");
                httpResponse.setStatusCode(HttpStatus.SC_OK);
                if (!file.exists()) {
                    retObject.put("error", "1");
                    retObject.put("message", "文件不存在");
                    retObject.put("action", mapRequest.get("action"));
                    entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                } else {
                    FileExtraUtils.delFile(file);
                    retObject.put("error", "0");
                    retObject.put("message", "操作成功");
                    retObject.put("action", mapRequest.get("action"));
                    entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                }
                Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
                httpResponse.setEntity(entity);
                return;
            }
            if (!file.exists()) {
                //文件不存在
                Log.d(TAG, "TAG file is not exist");
                FileExtraUtils.createDir(target);
                httpResponse.setStatusCode(HttpStatus.SC_OK);
                retObject.put("error", "0");
                retObject.put("message", "目录不存在,现已创建");
                retObject.put("action", mapRequest.get("action"));
                JSONArray jsonArray = new JSONArray();
                retObject.put("data", jsonArray);
                entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
                httpResponse.setEntity(entity);
                return;
            }

            //读取文件
            if (file.canRead()) {
                //获取文件总数
                if (ActionEnum.FILESUM.getCode().equals(mapRequest.get("action"))) {
                    Log.e(TAG, "TAG 查看文件总数");
                    httpResponse.setStatusCode(HttpStatus.SC_OK);
                    retObject.put("error", "0");
                    retObject.put("message", "操作成功");
                    retObject.put("action", mapRequest.get("action"));
                    int videoTotal = 0;
                    int fileTotal = 0;
                    int imageTotal = 0;
                    int musicTotal = 0;
                    File videoFile = new File(target + "/Videos");
                    if (!videoFile.exists()) {
                        FileExtraUtils.createDir(target);
                    } else {
                        List<String> templist = new ArrayList<String>();
                        List<String> list = ergodic(new File(target + "/Videos"),templist);
                        videoTotal = list.size();
                    }
                    File fileFile = new File(target + "/Files");
                    if (!fileFile.exists()) {
                        FileExtraUtils.createDir(target + "/Files");
                    } else {
                        List<String> templist = new ArrayList<String>();
                        List<String> list = ergodic(new File(target + "/Files"),templist);
                        fileTotal = list.size();
                    }
                    File imagesFile = new File(target + "/Pictures");
                    if (!imagesFile.exists()) {
                        FileExtraUtils.createDir(target + "/Pictures");
                    } else {
                        List<String> templist = new ArrayList<String>();
                        List<String> list = ergodic(new File(target + "/Pictures"),templist);
                        imageTotal = list.size();
                    }
                    File musicFile = new File(target + "/Musics");
                    if (!musicFile.exists()) {
                        FileExtraUtils.createDir(target + "/Musics");
                    } else {
                        List<String> templist = new ArrayList<String>();
                        List<String> list = ergodic(new File(target + "/Musics"),templist);
                        musicTotal = list.size();
                    }
                    retObject.put("videoTotal", videoTotal + "");
                    retObject.put("fileTotal", fileTotal + "");
                    retObject.put("imageTotal", imageTotal + "");
                    retObject.put("musicTotal", musicTotal + "");
                    entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                    Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
                    httpResponse.setEntity(entity);
                    return;
                }
                //获取文件列表
                httpResponse.setStatusCode(HttpStatus.SC_OK);
                if (file.isDirectory()) //如果是目录，实现文件夹浏览
                {
                    Log.e(TAG, "TAG 获取文件列表");
                    JSONArray jsonArray = new JSONArray();
                    findAllFiles(jsonArray, file);
                    retObject.put("error", "0");
                    retObject.put("action", mapRequest.get("action"));
                    retObject.put("message", "操作成功");
                    retObject.put("data", jsonArray.toJSONString());
                    entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
                    Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
                    httpResponse.setHeader("Content-Type", contentType);
                }

                httpResponse.setEntity(entity);
                return;
            }
            retObject.put("error", "1");
            retObject.put("action", mapRequest.get("action"));
            retObject.put("message", "没有权限读取");
            entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
            Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
            httpResponse.setStatusCode(HttpStatus.SC_FORBIDDEN);
        } catch (Exception e) {
            Log.e(TAG, "TAG" + e.getMessage());
            //缺少请求参数
            httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            JSONObject retObject = new JSONObject();
            retObject.put("error", "1");
            retObject.put("message", "服务异常" + e.getMessage());
            HttpEntity entity = new StringEntity(retObject.toJSONString(), Constant.ENCODING);
            Log.d(TAG, "TAG返回:" + EntityUtils.toString(entity));
            httpResponse.setEntity(entity);
            return;
        }

    }


    /**
     * 遍历所有文件
     *
     * @param jsonArray
     * @param dir
     * @throws Exception
     */
    private void findAllFiles(JSONArray jsonArray, File dir) {
        File[] fs = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return !file.isHidden() && !file.getName().startsWith(".");
            }
        });
        if (fs != null) {
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].isDirectory()) {
                    try {
                        findAllFiles(jsonArray, fs[i]);
                    } catch (Exception e) {
                    }
                } else {
                    if(!fs[i].getName().contains("readme")){
                        JSONObject retObject = new JSONObject();
                        retObject.put("name", fs[i].getName());
                        retObject.put("url", "http://"+ Utils.getLocalIpAddress() + ":" + Constant.Config.JETTYPORT+"/console" + fs[i].getAbsolutePath().replaceAll(Constant.Config.Web_Root, ""));
                        jsonArray.add(retObject);
                    }
                }
            }
        }
    }


    /**
     * 获取目录下所有文件数量
     *
     * @param sum
     * @param dir
     * @throws Exception
     */
    private void getFilesSum(int sum, File dir) {

        File[] fs = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return !file.isHidden() && !file.getName().startsWith(".");
            }
        });
        if (fs != null) {
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].isDirectory()) {
                    try {
                        getFilesSum(sum, fs[i]);
                    } catch (Exception e) {
                    }
                } else {
                    Log.e(TAG, "TAG getFilesSum name:" + fs[i].getName() + "absolutePath: " + fs[i].getAbsolutePath());
                    sum++;
                    Log.e(TAG, "TAG getFilesSum:" + sum);
                }
            }
        }
    }

    private List<String> ergodic(File file,List<String> resultFileName){
        File[] files = file.listFiles();
        if(files==null)return resultFileName;// 判断目录下是不是空的
        for (File f : files) {
            if(f.isDirectory()){// 判断是否文件夹
                //resultFileName.add(f.getPath());
                ergodic(f,resultFileName);// 调用自身,查找子目录
            }else{
                if(!f.getName().contains("readme")){
                    resultFileName.add(f.getPath());
                }
            }
        }
        return resultFileName;
    }

    /**
     * 获取文件数量
     *
     * @param path
     * @return
     */
    public int traverseFolder(String path, String type) {
        int fileNum = 0;
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<File>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    list.add(file2);
                    fileNum++;
                } else {
                    //folderNum++;
                }
            }
            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        list.add(file2);
                        fileNum++;
                    } else {
                        //folderNum++;
                    }
                }
            }
        } else {
            Log.e(TAG, "TAG 文件目录不存在:" +path);
        }
        return fileNum;
    }

    private String encodeUri(String uri) {
        String newUri = "";
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/"))
                newUri += "/";
            else if (tok.equals(" "))
                newUri += "%20";
            else {
                newUri += URLEncoder.encode(tok);
            }
        }
        return newUri;
    }

    private void write(File inputFile, OutputStream outStream) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        try {
            int count;
            byte[] buffer = new byte[Constant.BUFFER_LENGTH];
            while ((count = fis.read(buffer)) != -1) {
                outStream.write(buffer, 0, count);
            }
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            fis.close();
            outStream.close();
        }
    }

    private void write2(RandomAccessFile inputFile, OutputStream outStream) throws IOException {
        try {
        byte b[] = new byte[1024]; // 暂存容器
        int n = 0;
        while ((n = inputFile.read(b, 0, 1024)) != -1) {
            outStream.write(b, 0, n);
        }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            inputFile.close();
            outStream.close();
        }

        /*FileInputStream fis = new FileInputStream(inputFile);
        try {
            int count;
            byte[] buffer = new byte[Constant.BUFFER_LENGTH];
            while ((count = fis.read(buffer)) != -1) {
                outStream.write(buffer, 0, count);
            }
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            fis.close();
            outStream.close();
        }*/
    }


    private String encodeFilename(File file) throws IOException {
        String filename = URLEncoder.encode(getFilename(file), Constant.ENCODING);
        return filename.replace("+", "%20");
    }

    private String getFilename(File file) {
        return file.isFile() ? file.getName() : file.getName() + ".zip";
    }

    /**
     * 是否是图片
     *
     * @param url
     * @return
     */
    private boolean isImage(String url) {
        return url.matches("(?i).+?\\.(jpg|png)");
    }

    /**
     * 是否是视频
     *
     * @param url
     * @return
     */
    private boolean isVideo(String url) {
        return url.matches("(?i).+?\\.(mp4|mov|avi|rmvb|mkv)");
    }


    /**
     * 是否是文档
     *
     * @param url
     * @return
     */
    private boolean isDoc(String url) {
        return url.matches("(?i).+?\\.(doc|txt|excel|pdf|zip)");
    }

    /**
     * 是否是音乐
     *
     * @param url
     * @return
     */
    private boolean isMusic(String url) {
        return url.matches("(?i).+?\\.(mp3)");
    }

    /**
     * 是否是文件
     *
     * @param url
     * @return
     */
    private boolean isFile(String url) {
        return url.matches("(?i).+?\\.(mp3|doc|txt|excel|pdf|zip|mp4|mov|avi|rmvb|mkv|jpg|png)");
    }

}
