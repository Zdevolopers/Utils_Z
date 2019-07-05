package com.z.webmagic;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * 爬取图片工具包
 * 用jsoup捕捉页面的元素，并通过网络流 爬取图片
 * @author z
 * @version V1.0
 */
public class ImageWebMagic {
    /**
     * 下载图片到指定目录
     *
     * @param filePath 文件路径
     * @param imgUrl   图片URL
     */
    public static void downImages(String filePath, String imgUrl) {
        if(imgUrl == null || "".equals(imgUrl)){
            return;
        }
        // 若指定文件夹没有，则先创建
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 截取图片文件名
        String fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1, imgUrl.length());

        try {
            // 文件名里面可能有中文或者空格，所以这里要进行处理。但空格又会被URLEncoder转义为加号
            String urlTail = URLEncoder.encode(fileName, "UTF-8");
            // 因此要将加号转化为UTF-8格式的%20
            imgUrl = imgUrl.substring(0, imgUrl.lastIndexOf('/') + 1) + urlTail.replaceAll("\\+", "\\%20");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 写出的路径
        File file = new File(filePath + File.separator + fileName);

        try {
            // 获取图片URL
            URL url = new URL(imgUrl);
            // 获得连接
            URLConnection connection = url.openConnection();
            // 设置10秒的相应时间
            connection.setConnectTimeout(10 * 1000);
            // 获得输入流
            InputStream in = connection.getInputStream();
            // 获得输出流
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            // 构建缓冲区
            byte[] buf = new byte[1024];
            int size;
            // 写入到文件
            while (-1 != (size = in.read(buf))) {
                out.write(buf, 0, size);
            }
            out.close();
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        // 利用Jsoup获得连接
        Connection connect = Jsoup.connect("https://list.jd.com/list.html?cat=1315,1343,9719");
        try {
            // 得到Document对象
            Document document = connect.get();

            Elements images1 = document.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
            Elements images2 = document.select("img[data-lazy-img~=(?i)\\.(png|jpe?g|gif)]");
            System.out.println("--------images1 开始---------------");
            for (Element image : images1)
            {
                System.out.println("src : " + image.attr("src"));
                System.out.println("height : " + image.attr("height"));
                System.out.println("width : " + image.attr("width"));
                System.out.println("alt : " + image.attr("alt"));
            }
            System.out.println("--------images1 结束---------------");
            System.out.println("--------images2 开始---------------");
            for (Element image : images2)
            {
                System.out.println("src : " + image.attr("src"));
                System.out.println("height : " + image.attr("height"));
                System.out.println("width : " + image.attr("width"));
                System.out.println("alt : " + image.attr("alt"));
            }
            System.out.println("--------images2 结束---------------");
            System.out.println("size: "+ (images1.size()+images2.size()));



            /*// 查找所有img标签
            Elements lis = document.select("ul[class=gl-warp clearfix]").select("li[class=gl-item]");
            System.out.println("共检测到下列图片URL：");
            System.out.println("开始下载");
            System.out.println("**********************************************");
            // 遍历img标签并获得src的属性
            for (Element element : lis) {
                String imgSrc = element.select("div[class=gl-i-wrap j-sku-item]").select("div[class=p-img]").select("a").select("img").attr("abs:src");
                if("".equals(imgSrc) || imgSrc == null){
                    //获取每个img标签URL "abs:"表示绝对路径
                    imgSrc = element.select("div[class=gl-i-wrap j-sku-item]").select("div[class=p-img]").select("a").select("img").attr("abs:data-lazy-img");
                }
                System.out.println("src -- 》" + imgSrc);
                //下载图片到本地
                ImageWebMagic.downImages("F:/images/jd/skirts", imgSrc);
            }
            System.out.println("下载完成");*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
