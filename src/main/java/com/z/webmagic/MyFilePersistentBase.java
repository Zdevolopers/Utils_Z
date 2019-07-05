package com.z.webmagic;

import us.codecraft.webmagic.utils.FilePersistentBase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自定义的 爬取文件输出格式
 */
public class MyFilePersistentBase extends FilePersistentBase {

    private String fileName = "";
    //把date定义成成员变量，这样可以使得date的值不变，即只生成一个文件，并不会生成多个文件
    private Date date;
    private DateFormat dateFormat;

    public String fileNameByDate(){
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        date = new Date();
        fileName = dateFormat.format(date).replaceAll("-","");
        return fileName;
    }

    public static void main(String[] args) {
        MyFilePersistentBase base = new MyFilePersistentBase();
        String fileNameTest = base.fileNameByDate();
        System.out.println(fileNameTest);
        System.out.println(base.date.hashCode());
        System.out.println(new Date().hashCode());
    }

}
