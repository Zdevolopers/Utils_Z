package com.z.HuYi;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;

/**
 *  互亿无线工具类
 * @author z
 * @version V1.0
 */
public class HuyiUtils {

    /**
     * 接口类型：互亿无线触发短信接口，支持发送验证码短信、订单通知短信等。
     *     账户注册：请通过该地址开通账户http://user.ihuyi.com/register.html
     *     注意事项：
     *     （1）调试期间，请使用用系统默认的短信内容：您的验证码是：【变量】。请不要把验证码泄露给其他人。
     *    （2）请使用 APIID 及 APIKEY来调用接口，可在会员中心获取；
     *    （3）该代码仅供接入互亿无线短信接口参考使用，客户可根据实际需要自行编写；
     *    依赖：
            <dependency>
                <groupId>commons-httpclient</groupId>
                <artifactId>commons-httpclient</artifactId>
                <version>3.1</version>
            </dependency>
            <dependency>
                <groupId>dom4j</groupId>
                <artifactId>dom4j</artifactId>
                <version>1.6.1</version>
            </dependency>
     */
    private final String HUYI_URL = "http://106.ihuyi.cn/webservice/sms.php?method=Submit";
    private String HUYI_ACCOUNT = "C57206699";
    private String HUYI_PASSWORD= "2a4ce5c317c179d095f77855b368d253";
    private int HUYI_PRIVATE_CODE = (int) ((Math.random() * 9 + 1) * 100000);
    private String HUYI_CONTENT = "您的验证码是：" + HUYI_PRIVATE_CODE + "。请不要把验证码泄露给其他人。";

    public String sendMsg(String phone){
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(HUYI_URL);
        client.getParams().setContentCharset("GBK");
        method.setRequestHeader("ContentType", "application/x-www-form-urlencoded;charset=GBK");
        NameValuePair[] data = {//提交短信
                new NameValuePair("account", HUYI_ACCOUNT), //查看用户名 登录用户中心->验证码通知短信>产品总览->API接口信息->APIID
                new NameValuePair("password", HUYI_PASSWORD), //查看密码 登录用户中心->验证码通知短信>产品总览->API接口信息->APIKEY
                //new NameValuePair("password", util.StringUtil.MD5Encode("HUYI_PASSWORD")),
                new NameValuePair("mobile", phone),
                new NameValuePair("content", HUYI_CONTENT),
        };
        method.setRequestBody(data);
        String msg = "";
        try {
            client.executeMethod(method);
            String SubmitResult = method.getResponseBodyAsString();
            Document doc = DocumentHelper.parseText(SubmitResult);
            Element root = doc.getRootElement();
            String code = root.elementText("code");
            msg = root.elementText("msg");
            String smsid = root.elementText("smsid");
            System.out.println(code);
            System.out.println(msg);
            System.out.println(smsid);
            if ("2".equals(code)) { // 2表示验证成功 标识成功接收到验证短信
                return msg;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return msg;
    }

}
