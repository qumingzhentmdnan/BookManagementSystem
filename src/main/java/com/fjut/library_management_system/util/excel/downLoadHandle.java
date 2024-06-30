package com.fjut.library_management_system.util.excel;

import com.alibaba.fastjson2.JSON;
import com.fjut.library_management_system.util.Result;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

//下载文件通用处理
public class downLoadHandle {
    //设置Excel响应头
    public static void setExcelHead(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //使Content-disposition可以被前端访问
        response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
    }

    //处理异常
    public static void handleException(HttpServletResponse response,Exception e) throws IOException {
        // 检查响应是否已经被提交
        if (!response.isCommitted()) {
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
        }
        System.out.println(e.getMessage());
        response.getWriter().println(JSON.toJSONString(Result.error().message("下载文件失败")));
    }
}