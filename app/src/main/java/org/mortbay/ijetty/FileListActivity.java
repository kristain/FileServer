package org.mortbay.ijetty;

/**
 * Created by kristain on 16/3/15.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.Request;

import org.mortbay.ijetty.adapter.BaseAdapterHelper;
import org.mortbay.ijetty.adapter.QuickAdapter;
import org.mortbay.ijetty.entity.ActionEnum;
import org.mortbay.ijetty.entity.FileEntity;
import org.mortbay.ijetty.entity.FileTypeEnum;
import org.mortbay.ijetty.http.AsyncHttpResponseHandler;
import org.mortbay.ijetty.http.HttpClient;
import org.mortbay.ijetty.util.Constant;
import org.mortbay.ijetty.util.Utils;
import org.mortbay.ijetty.view.pulltorefresh.PullToRefreshBase;
import org.mortbay.ijetty.view.pulltorefresh.PullToRefreshListView;

import java.io.IOException;
import java.util.List;

/**
 * 主页面
 */
public class FileListActivity extends Activity {
    /**
     * 后退返回按钮
     */
    private Button btnBack;

    /**
     * 下拉加载列表组件
     */
    private PullToRefreshListView listView;

    private TextView textHeadTitle;

    private Bundle bundle;

    /**
     *
     */
    QuickAdapter<FileEntity> adapter;


    private TextView btnRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        bundle = getIntent().getExtras();
        initView();
        initEvent();

    }

    /**
     * 注册UI组件
     */
    private void initView() {
        btnRight = (TextView) findViewById(R.id.btnRight);
        listView = (PullToRefreshListView) findViewById(R.id.listView);
        btnBack = (Button) findViewById(R.id.btnBack);
        textHeadTitle = (TextView) findViewById(R.id.textHeadTitle);
    }

    /**
     * 注册事件
     */
    private void initEvent() {
        btnBack.setVisibility(View.VISIBLE);
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText("上传文件");
        textHeadTitle.setText(ActionEnum.getMsgByCode(bundle.getString("action")));

        /**
         * 后退返回键点击事件
         */
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /**
         * 点击上传文件
         */
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(FileListActivity.this, UploadFileActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);*/
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://" + Utils.getLocalIpAddress() + ":" + Constant.Config.UPLOADPORT);
                intent.setData(content_url);
                startActivity(intent);
            }
        });

        adapter = new QuickAdapter<FileEntity>(this, R.layout.file_list_item) {
            @Override
            protected void convert(BaseAdapterHelper helper, final FileEntity shop) {
                helper.setText(R.id.name, shop.getName())
                        .setText(R.id.address, shop.getUrl());
                //删除按钮
                helper.getView().findViewById(R.id.detele_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteFile(shop);
                    }
                });
            }
        };

        listView.addFooterView();
        listView.setAdapter(adapter);
        // 下拉刷新
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadData();
            }
        });
        // 加载更多
        listView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                loadData();
            }
        });
        loadData();

    }


    /**
     * 加载数据
     */
    private void loadData() {
        listView.setFooterViewTextNormal();
        FileEntity param = new FileEntity();
        param.setAction(bundle.getString("action"));
        HttpClient.postSend(param, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String body) {
                listView.onRefreshComplete();
                Log.i("postSendTAG", "成功返回数据:" + body);
                JSONObject object = JSON.parseObject(body);
                if (HttpClient.RET_SUCCESS_CODE.equals(object.getString("error"))) {
                    List<FileEntity> list = JSONArray.parseArray(object.getString("data"), FileEntity.class);
                    // 下拉刷新
                    if (adapter.getCount() != 0) {
                        adapter.clear();
                    }
                    // 暂无数据
                    if (list.isEmpty()) {
                        listView.setFooterViewTextNoData();
                        return;
                    }
                    adapter.addAll(list);
                    listView.setFooterViewTextNoMoreData();
                } else {
                    Log.i("postSendTAG", "获取数据失败:" + body);
                    Toast.makeText(FileListActivity.this, object.getString("message"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("postSendTAG", "获取数据失败:" + request.toString());
                listView.onRefreshComplete();
                listView.setFooterViewTextError();
            }
        }, this);
    }


    /**
     * 加载数据
     */
    private void deleteFile(FileEntity param) {
        param.setAction(ActionEnum.DELFILE.getCode());

        if(ActionEnum.FILELIST.getCode().equals(bundle.getString("action"))){
            param.setType(FileTypeEnum.FILE.getCode());
            param.setName(param.getUrl().substring(param.getUrl().indexOf(FileTypeEnum.FILE.getName())).replace(FileTypeEnum.FILE.getName() + "/", ""));
        }else if(ActionEnum.IMAGELIST.getCode().equals(bundle.getString("action"))){
            param.setType(FileTypeEnum.IMAGE.getCode());
            param.setName(param.getUrl().substring(param.getUrl().indexOf(FileTypeEnum.IMAGE.getName())).replace(FileTypeEnum.IMAGE.getName()+"/",""));
        }else if(ActionEnum.MUSICLIST.getCode().equals(bundle.getString("action"))){
            param.setType(FileTypeEnum.MUSIC.getCode());
            param.setName(param.getUrl().substring(param.getUrl().indexOf(FileTypeEnum.MUSIC.getName())).replace(FileTypeEnum.MUSIC.getName() + "/", ""));
        }else if(ActionEnum.VIDEOLIST.getCode().equals(bundle.getString("action"))){
            param.setType(FileTypeEnum.VIDEO.getCode());
            param.setName(param.getUrl().substring(param.getUrl().indexOf(FileTypeEnum.VIDEO.getName())).replace(FileTypeEnum.VIDEO.getName() + "/", ""));
        }
        HttpClient.deleteFile(param, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String body) {
                Log.i("deleteFileTAG", "成功返回数据:" + body);
                JSONObject object = JSON.parseObject(body);
                if (HttpClient.RET_SUCCESS_CODE.equals(object.getString("error"))) {
                    loadData();
                } else {
                    Log.i("deleteFileTAG", "删除文件失败:" + body);
                    Toast.makeText(FileListActivity.this, object.getString("message"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("deleteFileTAG", "获取数据失败:" + request.toString());
            }
        }, this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
