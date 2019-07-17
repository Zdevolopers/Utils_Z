package com.z.fdfs.fdfs2;

import com.github.tobato.fastdfs.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.MateData;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * FDFS的文件上传工具类二
 * @author  Z
 * @version  V1.0
 * @description 基于tobato和fdfs的依赖 封装的工具类
 * @dependence
        <dependency>
        <groupId>com.github.tobato</groupId>
        <artifactId>fastdfs-client</artifactId>
        <version>1.26.1-RELEASE</version>
        </dependency>
 */
@Component
public class TobatoFDFSUtils implements Serializable {

    private static final long serialVersionUID = 1L;
    private Logger log = LogManager.getLogger(TobatoFDFSUtils.class);

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private FdfsWebServer webServer;

    /**
     * 上传文件
     * @param file 文件对象 MultipartFile类型
     * @return 文件访问地址
     * @throws IOException
     */
    public String uploadFile(MultipartFile file) throws IOException {
        //开始计时
        long start = System.currentTimeMillis();

        InputStream inputStream = file.getInputStream();
        //上传
        StorePath storePath= storageClient.uploadFile(
                inputStream,
                file.getSize(),
                FilenameUtils.getExtension(file.getOriginalFilename()),
                null);
        //关闭资源
        if(inputStream != null){
            inputStream.close();
        }
        //结束计时
        long end = System.currentTimeMillis();
        log.info("上传-{} 一共用时-{}秒",file.getOriginalFilename(),(end-start)/1000.0000);
        return getResAccessUrl(storePath);
    }

    /**
     * 上传文件
     * @param file 文件对象 File类型
     * @return 文件访问地址
     * @throws IOException
     */
    public String uploadFile(File file) throws IOException {
        //开始计时
        long start = System.currentTimeMillis();

        FileInputStream inputStream = new FileInputStream(file.getName());
        StorePath storePath = storageClient.uploadFile(
                inputStream,
                file.length(),
                FilenameUtils.getExtension(file.getName()),
                null);
        //关闭资源
        if(inputStream != null){
            inputStream.close();
        }
        //结束计时
        long end = System.currentTimeMillis();
        log.info("上传-{} 一共用时-{}秒",file.getName(),(end-start)/1000.0000);
        return getResAccessUrl(storePath);
    }

    /**
     *  通过文件名称 上传文件
     * @param inputStream 文件输入流
     * @param size 文件大小
     * @param filename 文件名称
     * @return
     * @throws IOException
     */
    public String uploadFile(InputStream inputStream,Long size,String filename) throws IOException {
        //开始计时
        long start = System.currentTimeMillis();

        StorePath storePath = storageClient.uploadFile(
                inputStream,
                size,
                FilenameUtils.getExtension(filename),
                null);
        //结束计时
        long end = System.currentTimeMillis();
        log.info("上传-{} 一共用时-{}秒",filename,(end-start)/1000.0000);
        return getResAccessUrl(storePath);
    }

    /**
     *  指定一段内容 上传到文件上传服务器内
     * @param content  文件内容
     * @param fileExtention 文件扩展名
     * @return
     * @throws IOException
     */
    public String uploadFile(String content,String fileExtention) throws IOException {
        //开始计时
        long start = System.currentTimeMillis();

        byte[] buff = content.getBytes(Charset.forName("utf-8"));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buff);
        StorePath storePath = storageClient.uploadFile(
                inputStream,
                buff.length,
                fileExtention,
                null);
        //关闭资源
        if(inputStream != null){
            inputStream.close();
        }
        //结束计时
        long end = System.currentTimeMillis();
        log.info("上传-{} 一共用时-{}秒",content,(end-start)/1000.0000);
        return getResAccessUrl(storePath);
    }

    /**
     * 图片缩略图
     * @param is 输入流
     * @param size 文件大小
     * @param fileExtName 文件扩展名
     * @param metaData 元数据
     * @return 文件访问地址
        返回地址为：http://192.168.6.24:8977/group1/M00/00/00/wKgGGFnDGS-AVhrAAAMhasozgRc678.jpg
        访问缩略图地址为：http://192.168.6.24:8977/group1/M00/00/00/wKgGGFnDGS-AVhrAAAMhasozgRc678_150x150.jpg
     */
        public String upfileImage(InputStream is, long size, String fileExtName, Set<MateData> metaData){
            //开始计时
            long start = System.currentTimeMillis();

            StorePath path = storageClient.uploadImageAndCrtThumbImage(is,size,fileExtName,metaData);
            //结束计时
            long end = System.currentTimeMillis();
            log.info("上传 一共用时-{}秒",(end-start)/1000.0000);
            return getResAccessUrl(path);
    }

    /**
     * 通过 文件上传服务器返回的标识
     *  拼接出 文件的访问路径
     * @param storePath
     * @return
     */
    private String getResAccessUrl(StorePath storePath){
        return webServer.getWebServerUrl() + storePath.getFullPath();
    }

    /**
     *  根据 文件的访问路径 获取文件的下载路径和组名称
     *   暂时 先考虑只 http/https协议下上传下载
     *   暂时 先考虑只上传和下载 gif/png/jpg/img/txt/zip/exe文件
     * @param url
     * @return
     */
    private Map<String,String> getDownUrl(String url){
        //格式如："http://192.168.3.147/group1/M00/00/00/wKgDk10r6HSAZ_mZAABzEZjOE_c277.jpg"
        if(url.matches("(http|https):\\/\\/.*?(gif|png|jpg|txt|zip|img|exe)")){
            Map<String,String> map = new HashMap<>();
            String[] results = url.trim().split("/");
            //截取出协议
            String protocol = results[0];
            //截取出服务器IP
            String ip = results[2];
            //截取出文件所在的组
            String group = results[3];
            //截取出文件的路径
            String path = results[4]+"/"+results[5]+"/"+results[6]+"/"+results[7];
            log.info("协议为：{}，ip为：{}，组名称为：{}，文件路径为：{}",protocol,ip,group,path);
            //存入map中
            map.put("protocol",protocol);
            map.put("ip",ip);
            map.put("group",group);
            map.put("path",path);
            return map;
        }
        return null;
    }

    /**
     * 根据 文件的访问路径  下载到指定的文件夹中
     * @param url 文件访问地址
     * @param fileName 文件名称：为空则随机产生文件名称
     * @param local  文件下载储存地址（本地）
     * @return
     */
    public Boolean download(String url,String fileName,String local){
        Map<String, String> map = getDownUrl(url);
        if(map != null){
            //开始计时
            long start = System.currentTimeMillis();

            String group = map.get("group");
            String path = map.get("path");
            byte[] bytes = storageClient.downloadFile(group, path, new DownloadByteArray());
            //开始下载
            FileOutputStream out = null;
            try {
                if(fileName == null || "".equals(fileName)){
                    out = new FileOutputStream(local + "\\"+ UUID.randomUUID()+url.substring(url.lastIndexOf(".")));
                }else{
                    out = new FileOutputStream(local + "\\"+ fileName);
                }
                out.write(bytes);

                //结束计时
                long end = System.currentTimeMillis();
                log.info("下载-{} 一共用时-{}秒",fileName,(end-start)/1000.0000);
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(out != null){
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     *  通过文件访问路径 删除上传的文件
     * @param fileUrl  文件访问路径
     * @return
     */
    public String delete(String fileUrl){
        if(StringUtils.isEmpty(fileUrl)){
            return "文件路径为空，删除失败";
        }
        try {
            //开始计时
            long start = System.currentTimeMillis();

            StorePath storePath = StorePath.praseFromUrl(fileUrl);
            //删除时，需要参数： （从访问路径分解下来的） 组名 和 路径
            storageClient.deleteFile(storePath.getGroup(),storePath.getPath());

            //结束计时
            long end = System.currentTimeMillis();
            log.info("删除-{} 一共用时-{}秒",fileUrl,(end-start)/1000.0000);
            return "删除成功";
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return "参数异常，删除失败";
    }




}
