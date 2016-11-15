package org.mortbay.ijetty.entity;

import org.mortbay.ijetty.util.StringUtils;

/**
 * Created by kristain on 16/3/9.
 */
public enum ActionEnum {

    FILESUM("100010", "获取设备文件数量"),
    VIDEOLIST("100011", "获取设备视频列表"),
    FILELIST("100012", "获取设备文件列表"),
    IMAGELIST("100013", "获取设备图片列表"),
    MUSICLIST("100014", "获取设备音乐列表"),
    DELFILE("100015", "删除文件"),
    UPLOADFILE("100016", "上传文件");

    public final String code;
    public final String name;

    ActionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static boolean isAction(String code) {
        if (StringUtils.isEmpty(code)) {
            return false;
        }
        for (ActionEnum s : ActionEnum.values()) {
            if (s.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据value值获取key
     *
     * @param msg
     * @return
     */
    public static String getCodeByMsg(String msg) {
        for (ActionEnum v : ActionEnum.values()) {
            if (v.getName().equals(msg)) {
                return v.getCode();
            }
        }
        return "";
    }

    /**
     * 根据key值获取value
     *
     * @param code
     * @return
     */
    public static String getMsgByCode(String code) {
        for (ActionEnum v : ActionEnum.values()) {
            if (v.getCode().equals(code)) {
                return v.getName();
            }
        }
        return "";
    }
}