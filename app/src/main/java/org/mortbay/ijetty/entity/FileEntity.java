package org.mortbay.ijetty.entity;


import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * Created by kristain on 16/3/15.
 */
public class FileEntity implements Serializable {

    private String id;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 文件路径
     */
    private String url;

    private String action;

    private String error;
    private String message;
    private String videoTotal;
    private String fileTotal;
    private String imageTotal;
    private String musicTotal;

    private String data;

    /**
     * 文件类型
     */
    private String type;


    private String saveDir;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件数据
     */
    private String file;


    public FileEntity copy() {
        return JSON.parseObject(JSON.toJSONString(this), FileEntity.class);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVideoTotal() {
        return videoTotal;
    }

    public void setVideoTotal(String videoTotal) {
        this.videoTotal = videoTotal;
    }

    public String getFileTotal() {
        return fileTotal;
    }

    public void setFileTotal(String fileTotal) {
        this.fileTotal = fileTotal;
    }

    public String getImageTotal() {
        return imageTotal;
    }

    public void setImageTotal(String imageTotal) {
        this.imageTotal = imageTotal;
    }

    public String getMusicTotal() {
        return musicTotal;
    }

    public void setMusicTotal(String musicTotal) {
        this.musicTotal = musicTotal;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
