package com.wangpu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@Controller
public class UploadController {
    //Save the uploaded file to this folder

    /**
     * 初始化上传文件界面，跳转到upload.html
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
    public String index() {
        return "upload";
    }

    /**
     * 提取上传方法为公共方法
     * @param uploadDir 上传文件目录
     * @param file 上传对象
     * @throws Exception
     */
    private void executeUpload(String uploadDir,MultipartFile file,RedirectAttributes redirectAttributes) throws Exception
    {
        //文件后缀名
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        //上传文件名
        String filename = UUID.randomUUID() + suffix;
        //服务器端保存的文件对象
        File serverFile = new File(uploadDir + filename);
        //将上传的文件写入到服务器端文件内
        file.transferTo(serverFile);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded '" + file.getOriginalFilename() + "'");
    }

    /**
     * 上传文件方法
     * @param file 前台上传的文件对象
     * @return
     */
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    String upload(HttpServletRequest request, MultipartFile file,RedirectAttributes redirectAttributes)
    {
        try {

            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
                return "redirect:uploadStatus";
            }
            //上传目录地址
            String uploadDir = request.getSession().getServletContext().getRealPath("/") +"upload/";
            //如果目录不存在，自动创建文件夹
            File dir = new File(uploadDir);
            if(!dir.exists()) {
                dir.mkdir();
            }
            //调用上传方法
            executeUpload(uploadDir,file,redirectAttributes);
        }catch (Exception e)
        {
            //打印错误堆栈信息
            e.printStackTrace();
        }

        return "redirect:/uploadStatus";
    }

    /**
     * 上传多个文件
     * @param request 请求对象
     * @param file 上传文件集合
     * @return
     */
    @RequestMapping(value = "/uploads",method = RequestMethod.POST)
    public String uploads(HttpServletRequest request,@RequestParam("file") MultipartFile[] file,RedirectAttributes redirectAttributes)
    {

        if (file.length<0) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {


            //上传目录地址
            String uploadDir = request.getSession().getServletContext().getRealPath("/") +"upload/";
            //如果目录不存在，自动创建文件夹
            File dir = new File(uploadDir);
            if(!dir.exists())
            {
                dir.mkdir();
            }
            //遍历文件数组执行上传
            for (int i =0;i<file.length;i++) {
                if(file[i] != null) {
                    //调用上传方法
                    executeUpload(uploadDir, file[i],redirectAttributes);
                }
            }
        }catch (Exception e)
        {
            //打印错误堆栈信息
            e.printStackTrace();
        }
        return "redirect:/uploadStatus";
    }


    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }



    @RequestMapping(value = "/downloadfile", method = RequestMethod.GET)
    @ResponseBody
    public String testDownload(HttpServletResponse res) {
        String fileName = "MybatisXML.png";
        res.setHeader("content-type", "application/octet-stream");
        res.setContentType("application/octet-stream");
        res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            os = res.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(new File("/Users/Kenvin/Desktop/"
                    + fileName)));
            int i = bis.read(buff);
            while (i != -1) {
                os.write(buff, 0, buff.length);
                os.flush();
                i = bis.read(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "下载失败";
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return "下载失败";
                }
            }
        }
        return "下载成功";
    }
}