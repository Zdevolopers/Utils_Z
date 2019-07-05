package com.z.webmagic;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 爬取 电影网站资源
 * WebMagic框架实现 网络爬虫
 * @author z
 * @apiNote 爬取资源
 * @version V1.0
 */
public class MovieWebMagic implements PageProcessor {

    /**
     * Site: 一般就是要分析当前的网页
     * 抓取 网站的相关配置：编码、重试次数、抓取间隔等
     *  为防止该页面请请求失败
     *  setRetryTimes(3)设置该页面请求次数
     *  setSleepTime(3000)设置请求次数间隔时间
     */
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        //电影详情链接movieLink的正则表达式
        String movieLinkReg="/html/gndy/\\w{4}/\\d{8}/\\d{5}.html";
        Pattern movieLinkPattern= Pattern.compile(movieLinkReg);
        //写相应的xpath
        String movieNameXpath="//title/text()";
        String movieDownloadXpath="//a[starts-with(@href,'ftp')]/text()";
        String movieLinkXpath="//div[@class='co_content2']/ul/a[@href]";
        List<String> movieLinkList=new ArrayList<String>();
        //结果抽取
        Selectable moviePage;
        Selectable movieNameS;
        Selectable movieDownloadS;
        if("http://www.dytt8.net".equals(page.getUrl().toString())){
            //抽取结果
            moviePage=page.getHtml().xpath(movieLinkXpath);
            //选中结果
            movieLinkList=moviePage.all();
            //循环遍历
            String movieLink="";
            Matcher movieLinkMatcher;
            for(int i=1;i<10;i++){
                //第一条过滤，从第二条开始遍历
                //网页的编码是gb2312，现在应该把编码 设置成utf-8
                try {
                    movieLink = getContent(movieLinkList.get(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //正则匹配
                movieLinkMatcher=movieLinkPattern.matcher(movieLink);
                if(movieLinkMatcher.find()){//匹配子串
                    movieLink=movieLinkMatcher.group();//返回匹配到的字串
                    //将找到的链接放到ddTargetRequest里面，会自动发起请求
                    page.addTargetRequest(movieLink);
                    //输出到控制台
                    System.out.println(movieLink);
                }
            }
        }else{//第二次请求，电影详情页面
            //获取html
            movieNameS=page.getHtml().xpath(movieNameXpath);
            movieDownloadS=page.getHtml().xpath(movieDownloadXpath);
            page.putField("movieName",page.getHtml().xpath("//title/text()").toString());
            page.putField("downloadURL", page.getHtml().xpath("//a[starts-with(@href,'ftp')]/text()").toString());

            //System.out.println("movieName: "+ movieNameS);
            //System.out.println("movieDownloadS: "+ movieDownloadS);
        }
        movieLinkList.clear();

       /* *//**
         * 抓取匹配符合该正则表达式的URL加入请求队列中
         * 	.all()--返回的是个集合
         *//*
        page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/\\w+/\\w+)").all());
        *//**
         * 抽取结果元素
         *//*
        page.putField("author", page.getUrl().regex("\"https://github\\.com/(\\w+)/.*\"").toString());
        page.putField("name", page.getHtml().xpath("//h1[@class='entry-title public']/strong/a/text()").toString());
        if (page.getResultItems().get("name") == null){
            page.setSkip(true);
        }
        page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"));*/
    }

    public static void main(String[] args) {
        //爬取的路径URL---1个线程去执行
        //通过Pipeline持久化数据（持久化成多个json文件）
        //Spider.create(new webMagicUtils()).addUrl("http://www.dytt8.net").addPipeline(new JsonFilePipeline("F://WEBMAGIC_FILE")).thread(1).run();
        //持久化成自定义模式的文件
        Spider.create(new MovieWebMagic()).addUrl("http://www.dytt8.net").addPipeline(new MyJsonFilePipeline("F://WEBMAGIC_FILE")).thread(1).run();
        //或者--也是输出到控制台
        //Spider.create(new webMagicUtils()).addUrl("http://www.dytt8.net").addPipeline(new ConsolePipeline()).run();


        /**
         * Spider入口
         * 	 addUrl--添加爬取的地址
         * 	 thread(5).run()--5个线程去抓取
         *//*
        Spider.create(new GithubRepoPageProcessor())
                .addUrl("https://github.com/code4craft")
                //覆盖默认的实现 HttpClientDownloader
                .setDownloader(new HttpClientDownloader())
                .thread(1)
                .run();*/
    }

    public String getContent(String html) throws Exception {
        byte[] bytes = html.getBytes();
        String content = new String(bytes);
        // 默认为utf-8编码
        String charset = "utf-8";
        // 匹配<head></head>之间，出现在<meta>标签中的字符编码
        String style = "<head>([\\s\\S]*?)<meta([\\s\\S]*?)charset\\s*=(\")?(.*?)\"";
        Pattern pattern = Pattern.compile(style);
        Matcher matcher = pattern.matcher(content.toLowerCase());
        if (matcher.find()) {
            charset = matcher.group(4);
            if (charset.equals("gb2312")) {
                byte[] gbkBytes = new String(bytes, "gbk").getBytes();
                return new String(gbkBytes, "utf-8");
            }
        }
        // 将目标字符编码转化为utf-8编码
        String temp = new String(bytes, charset);
        //byte[] contentData = temp.getBytes("utf-8");
        return temp;
    }

}
