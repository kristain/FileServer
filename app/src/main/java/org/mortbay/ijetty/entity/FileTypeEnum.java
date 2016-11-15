package org.mortbay.ijetty.entity;

import org.mortbay.ijetty.util.StringUtils;

/**
 * 文件类型
 * Created by kristain on 16/3/9.
 */
public enum FileTypeEnum {
    VIDEO("1", "Videos"),
    FILE("2", "Files"),
    IMAGE("3", "Pictures"),
    MUSIC("4", "Musics"),
    UPDATA("5","Update");

    public final String code;
    public final String name;

    FileTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static boolean isFileType(String code) {
        if (StringUtils.isEmpty(code)) {
            return false;
        }
        for (FileTypeEnum s : FileTypeEnum.values()) {
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
        for (FileTypeEnum v : FileTypeEnum.values()) {
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
        for (FileTypeEnum v : FileTypeEnum.values()) {
            if (v.getCode().equals(code)) {
                return v.getName();
            }
        }
        return "";
    }
}
