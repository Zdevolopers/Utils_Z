package com.z.webmagic;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义爬取管道：用来定义输出格式
 */
public class MyJsonFilePipeline extends MyFilePersistentBase implements Pipeline {

    private Logger logger;

    public MyJsonFilePipeline(String path) {
        logger = LogManager.getLogger(MyJsonFilePipeline.class);
        setPath(path);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        try {
            if(!CollectionUtils.isEmpty(resultItems.getAll())){
                String filePath =  new StringBuilder().append(path).append(PATH_SEPERATOR)
                        .append(fileNameByDate()).append(".txt").toString();
                //true 表示不将原先的内容覆盖
                PrintWriter printWriter = new PrintWriter(new FileWriter(getFile(filePath),true));
                //printWriter.write(JSON.toJSONString(resultItems.getAll()));
                //格式
                printWriter.write("电影名称：->"+(String) resultItems.getAll().get("movieName"));
                printWriter.write(System.getProperties().getProperty("line.separator"));
                printWriter.write("下载地址：->"+(String) resultItems.getAll().get("downloadURL"));
                printWriter.write(System.getProperties().getProperty("line.separator"));
                printWriter.write(System.getProperties().getProperty("line.separator"));
                printWriter.close();
            }
        } catch (IOException e) {
            logger.warn("write file error --> ", e);
            //System.out.println("文件写入出异常！！！！");
        }
    }
}
