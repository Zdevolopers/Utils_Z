package com.z.fdfs.fdfs1;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *  FDFS的文件上传工具类一
 * @author  Z
 * @version V1.0
 * @description  区别与 Tobato文件上传工具类
 * @dependence
        <dependency>
        <groupId>org.csource</groupId>
        <artifactId>fastdfs-client-java</artifactId>
        <version>1.27-SNAPSHOT</version>
        </dependency>

        <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.4</version>
        </dependency>
 */
public class FDFSUtils implements Serializable {

    private static final long serialVersionUID = 1L;
    private Logger log = LogManager.getLogger(FDFSUtils.class);

    private TrackerClient trackerClient;
    private TrackerServer trackerServer;
    private StorageServer storageServer;
    private StorageClient1 storageClient;

    /**
     * 通过构造方法，传入fdfs配置文件，初始化storage和tracker的连接
     * @param conf  (resources/config/fdfs_client.conf)
     * @throws Exception
     */
    public FDFSUtils(String conf) throws Exception {
        if (conf.contains("classpath:")) {
            conf = conf.replace("classpath:", this.getClass().getResource("/").getPath());
        }
        ClientGlobal.init(conf);
        trackerClient = new TrackerClient();
        trackerServer = trackerClient.getConnection();
        storageServer = null;
        storageClient = new StorageClient1(trackerServer, storageServer);
    }

    /**
     * 上传文件方法
     * @param fileName 文件全路径
     * @param extName 文件扩展名，不包含（.）
     * @param metas 文件扩展信息
     * @return
     * @throws Exception
     */
    public String uploadFile(String fileName, String extName, NameValuePair[] metas) throws Exception {
        //开始计时
        long start = System.currentTimeMillis();

        String result = storageClient.upload_file1(fileName, extName, metas);
        //结束计时
        long end = System.currentTimeMillis();
        log.info("上传-{} 一共用时-{}秒",fileName,(end-start)/1000.0000);
        return result;
    }

    /**
     * 上传文件方法
     * @param fileContent 文件的内容，字节数组
     * @param extName 文件扩展名
     * @param metas 文件扩展信息
     * @return
     * @throws Exception
     */
    public String uploadFile(byte[] fileContent, String extName, NameValuePair[] metas) throws Exception {
        //开始计时
        long start = System.currentTimeMillis();

        String result = storageClient.upload_file1(fileContent, extName, metas);
        //结束计时
        long end = System.currentTimeMillis();
        log.info("上传 一共用时-{}秒",(end-start)/1000.0000);
        return result;
    }

    /**
     *  上传文件（包含 分块上传）
     * @param file  MultipartFile文件
     * @param extName  文件的扩展名
     * @param metas  元数据信息
     * @return  文件的访问地址，缺少协议和ip（由yml配置文件中获取）
     * @throws Exception
     */
    public String uploadFile(MultipartFile file, String extName, NameValuePair[] metas) throws Exception {
        //开始计时
        long start = System.currentTimeMillis();

        //默认长度
        long defaultSize = 250 * 1024;
        //文件长度
        long fileSize = file.getSize();
        //文件地址的字符数组
        String[] result = null;
        if(fileSize > defaultSize){
            //默认分块时的指针
            int pos = 1;
            //分块长度
            int count = 0;
            //分块上传
            count = fileSize % defaultSize == 0 ? (int)(fileSize/defaultSize) : (int)((fileSize/defaultSize)+ 1);
            while(pos < count + 1){
                if(pos < count) {
                    if (pos == 1) {
                        //第一次上传
                        result = storageClient.upload_appender_file(file.getBytes(),0,(int)defaultSize, extName, metas);
                    } else {
                        //开始追加
                        storageClient.append_file(result[0], result[1], file.getBytes(),0,(int)defaultSize);
                    }
                }else{
                    //不足一个defaultSize(含)的最后一块
                    storageClient.append_file(result[0], result[1], file.getBytes(),0,(int)(fileSize - (pos - 1) * defaultSize));
                }
                pos++;
            }
            //需要写入内容
            storageClient.modify_file(result[0],result[1],0,file.getBytes(),0,file.getBytes().length);
        }else {
            //一次性上传
            result = storageClient.upload_file(file.getBytes(), extName, metas);
        }
        String url = result[0]+"/"+result[1];
        //结束计时
        long end = System.currentTimeMillis();
        log.info("上传-{} 一共用时-{}秒",file.getOriginalFilename(),(end-start)/1000.0000);
        return url;
    }

    /**
     *  下载
     * @param url  文件访问路径
     * @param fileName  文件名称
     * @param local  上传到本地的文件位置
     * @return
     */
    public String download(String url,String fileName,String local){
        Map<String, String> map = getDownUrl(url);
        if(map != null){
            //开始计时
            long start = System.currentTimeMillis();

            FileOutputStream out = null;
            String group = map.get("group");
            String path = map.get("path");
            try {
                byte[] bytes = storageClient.download_file(group, path);
                //开始写入
                out = new FileOutputStream(local + "\\" + fileName);
                out.write(bytes);
            } catch (IOException e) {
                log.error("下载失败",e.getMessage());
            } catch (MyException e) {
                e.printStackTrace();
                return "下载失败";
            }finally {
                if (out != null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //结束计时
            long end = System.currentTimeMillis();
            log.info("下载-{} 一共用时-{}秒",fileName,(end-start)/1000.0000);
            return "下载成功";
        }
        return url+"参数不匹配";
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



}
