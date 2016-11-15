package fi.iki.elonen.util;

/**
 *
 */
public abstract class DefaultHtml {
    public static String HTML_STRING = "<html>"
            + "<head><meta charset=\"UTF-8\"><title>文件上传</title></head>"
            + "<body>"
            + "<div style=\"margin:atuo;margin-top:50px;text-align:center;\">"
            + "<form action=\"\" name=\"airForm\" method=\"post\" enctype=\"multipart/form-data\">"
            + "<label>目录<input type=\"text\" name=\"saveDir\" value=\"test\"/></label>"
            + "<label>文件类型<input type=\"text\" name=\"fileType\" value=\"2\"/></label>"
            + "<label><input type=\"file\" name=\"file\"/></label>"
            + "<input type=\"submit\" name=\"button\" id=\"button\" value=\"上传\" />"
            + "</form></div></body></html>";

}
