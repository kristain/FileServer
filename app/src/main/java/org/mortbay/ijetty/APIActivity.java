package org.mortbay.ijetty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.Request;

import org.mortbay.ijetty.entity.ActionEnum;
import org.mortbay.ijetty.entity.FileEntity;
import org.mortbay.ijetty.http.AsyncHttpResponseHandler;
import org.mortbay.ijetty.http.HttpClient;

import java.io.IOException;


/**
 * Created by kristain on 16/3/15.
 */
public class APIActivity extends Activity{
    private Button dev_file_sum,dev_file_list,video_file_list,image_file_list,music_file_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_controller);
        dev_file_sum = (Button)findViewById(R.id.dev_file_sum);
        video_file_list= (Button)findViewById(R.id.video_file_list);
        image_file_list= (Button)findViewById(R.id.image_file_list);
        music_file_list= (Button)findViewById(R.id.music_file_list);
        dev_file_list = (Button)findViewById(R.id.dev_file_list);
        initEvent();
    }


    /**
     * 注册事件
     */
    private void initEvent(){
        /**
         * 点击获取文件数量
         */
        dev_file_sum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFileSum();
            }
        });

        /**
         * 点击获取文件列表
         */
        dev_file_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(APIActivity.this,FileListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("action", ActionEnum.FILELIST.getCode());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        /**
         * 点击获取视频列表
         */
        video_file_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(APIActivity.this,FileListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("action", ActionEnum.VIDEOLIST.getCode());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        /**
         * 点击获取图片列表
         */
        image_file_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(APIActivity.this,FileListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("action", ActionEnum.IMAGELIST.getCode());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        /**
         * 点击获取音乐列表
         */
        music_file_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(APIActivity.this, FileListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("action", ActionEnum.MUSICLIST.getCode());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

    /**
     * 获取文件数量
     */
    private void getFileSum() {
        FileEntity param = new FileEntity();
        param.setAction(ActionEnum.FILESUM.getCode());
        HttpClient.postSend(param, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String body) {
                Log.i("postSendTAG", "成功返回数据:" + body);
                JSONObject object = JSON.parseObject(body);
                if (HttpClient.RET_SUCCESS_CODE.equals(object.getString("error"))) {
                    Toast.makeText(APIActivity.this, object.toString(), Toast.LENGTH_LONG).show();
                } else {
                    Log.i("postSendTAG", "获取文件数量失败:" + body);
                    Toast.makeText(APIActivity.this, object.getString("message"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("postSendTAG", "获取数据失败:" + request.toString());
            }
        }, this);
    }
}
