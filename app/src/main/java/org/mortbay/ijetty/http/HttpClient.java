package org.mortbay.ijetty.http;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mortbay.ijetty.GlobalApplication;
import org.mortbay.ijetty.R;
import org.mortbay.ijetty.entity.FileEntity;
import org.mortbay.ijetty.util.Constant;
import org.mortbay.ijetty.util.StringUtils;
import org.mortbay.ijetty.util.Utils;
import org.mortbay.ijetty.view.dialog.LoadingProgressDialog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP请求类
 * Created by kristain on 15/2/27.
 */
public class HttpClient {

    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    public static final String USER_NAME = "kristain";
    public static final String UTF_8 = "UTF-8";
    public static final int PAGE_SIZE = 20;

    public static final String HTTP_DOMAIN ="http://" + Utils.getLocalIpAddress() + ":" + Constant.Config.VIEWPORT;
    public static final String HTTP_UPLOAD_DOMAIN="http://" + Utils.getLocalIpAddress() + ":" + Constant.Config.UPLOADPORT;
   // private static final String HTTP_DOMAIN = "http://sye.zhongsou.com/ent/rest";

    private static final String DOLOGIN = "/";//首页动态数据


    public static final String RET_SUCCESS_CODE="0";
    public static final String UNLOGIN_CODE="1000";


    private static LoadingProgressDialog proDialog = null;
    static {
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        mOkHttpClient.setCookieHandler(new CookieManager(new PersistentCookieStore(GlobalApplication.getInstance()), CookiePolicy.ACCEPT_ALL));
        //proDialog = LoadingProgressDialog.getDialog();
    }

    /**
     * 不会开启异步线程。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static Response execute(Request request) throws IOException {
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 开启异步线程访问网络
     *
     * @param request
     * @param httpResponseHandler
     */
    public static void enqueue(Request request, final AsyncHttpResponseHandler httpResponseHandler,Context mcontext, final boolean load_flag) {
        if(load_flag){
            startProgressDialog("加载中",mcontext);
        }
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Response response) throws IOException {
                httpResponseHandler.sendSuccessMessage(response);
                if(load_flag) {
                    stopProgressDialog();
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                httpResponseHandler.sendFailureMessage(request, e);
                if(load_flag) {
                    stopProgressDialog();
                }
            }
        });
    }

    /**
     * 开启异步线程访问网络, 且不在意返回结果（实现空callback）
     *
     * @param request
     */
    public static void enqueue(Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Response arg0) throws IOException {

            }

            @Override
            public void onFailure(Request arg0, IOException arg1) {

            }
        });
    }

    public static String getStringFromServer(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = execute(request);
        if (response.isSuccessful()) {
            String responseUrl = response.body().string();
            return responseUrl;
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }

    /**
     * 这里使用了HttpClient的API。只是为了方便
     *
     * @param params
     * @return
     */
    public static String formatParams(List<BasicNameValuePair> params) {
        return URLEncodedUtils.format(params, UTF_8);
    }

    /**
     * 为HttpGet 的 url 方便的添加多个name value 参数。
     *
     * @param url
     * @param params
     * @return
     */
    public static String attachHttpGetParams(String url, List<BasicNameValuePair> params) {
        return url + "?" + formatParams(params);
    }

    /**
     * 为HttpGet 的 url 方便的添加1个name value 参数。
     *
     * @param url
     * @param name
     * @param value
     * @return
     */
    public static String attachHttpGetParam(String url, String name, String value) {
        return url + "?" + name + "=" + value;
    }

    public static boolean isNetworkAvailable() {
       /* try {
            ConnectivityManager connectivityManager = (ConnectivityManager) GlobalApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
        } catch (Exception e) {
            Log.v("Connectivity", e.getMessage());
        }
        return false;*/
        return true;
    }

    private static String encodeParams(Map<String, Object> params) {
        String param = "{}";
        if (params != null) {
            JSONObject json = new JSONObject();
            try {
                for (String key : params.keySet()) {
                    json.put(key, params.get(key));
                }
                Log.i("net_params", json.toString());
                param = Base64.encodeToString(json.toString().getBytes(UTF_8), Base64.DEFAULT);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return param;
    }


    public static void get(String url, Map<String, Object> params, AsyncHttpResponseHandler httpResponseHandler,Context mcontext,final boolean load_flag) {
        if (!isNetworkAvailable()) {
            Toast.makeText(GlobalApplication.getInstance(), R.string.no_network_connection_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i("GET请求交易TAG", "请求url:" + url + "请求参数:" + JSON.toJSONString(params));
        List<BasicNameValuePair> rq = new ArrayList<BasicNameValuePair>();
        if (params != null) {
            for (String key : params.keySet()) {
                rq.add(new BasicNameValuePair(key, params.get(key).toString()));
            }
        }
        Request request = new Request.Builder().url(attachHttpGetParams(HTTP_DOMAIN + url, rq)).build();
        enqueue(request, httpResponseHandler,mcontext,load_flag);
    }


    public static void post(String url, Map<String, Object> params, AsyncHttpResponseHandler httpResponseHandler,Context mcontext) {
        if (!isNetworkAvailable()) {
            Toast.makeText(mcontext, R.string.no_network_connection_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i("Post请求交易TAG", "请求url:" + url + "请求参数:" + JSON.toJSONString(params));
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params != null) {
            for (String key : params.keySet()) {
                builder.add(key,params.get(key).toString());
            }
        }
        Request request = new Request.Builder().url(HTTP_DOMAIN + url).post(builder.build()).build();
        enqueue(request, httpResponseHandler,mcontext,true);
    }




    /**
     * 获取文件列表 、数量
     * @param param
     * @param httpResponseHandler
     */
    public static void postSend(FileEntity param, AsyncHttpResponseHandler httpResponseHandler,Context mcontext) {
        Map<String, Object> params = new HashMap<String, Object>();
        if(!StringUtils.isEmpty(param.getAction())){
            params.put("action", param.getAction());
        }
        if(!StringUtils.isEmpty(param.getType())){
            params.put("type",param.getType());
        }
        if(!StringUtils.isEmpty(param.getId())){
            params.put("id", param.getId());
        }
        if(!StringUtils.isEmpty(param.getSaveDir())){
            params.put("saveDir",param.getSaveDir());
        }
        if(!StringUtils.isEmpty(param.getFileType())){
            params.put("fileType",param.getFileType());
        }
        post(DOLOGIN, params, httpResponseHandler, mcontext);
    }


    /**
     * 删除文件
     * @param param
     * @param httpResponseHandler
     */
    public static void deleteFile(FileEntity param, AsyncHttpResponseHandler httpResponseHandler,Context mcontext) {
        Map<String, Object> params = new HashMap<String, Object>();
        if(!StringUtils.isEmpty(param.getAction())){
            params.put("action", param.getAction());
        }
        if(!StringUtils.isEmpty(param.getType())){
            params.put("type",param.getType());
        }
        if(!StringUtils.isEmpty(param.getName())){
            params.put("name",param.getName());
        }
        post(DOLOGIN, params, httpResponseHandler, mcontext);
    }




    private static void startProgressDialog(String progressMsg,Context mcontext) {
        if (proDialog == null) {
            proDialog = LoadingProgressDialog.createDialog(mcontext);
            proDialog.setMessage(progressMsg);
            proDialog.setCanceledOnTouchOutside(false);
        }
        proDialog.show();
    }

    private static void stopProgressDialog() {
        if (proDialog != null) {
            proDialog.dismiss();
            proDialog = null;
        }
    }



}
