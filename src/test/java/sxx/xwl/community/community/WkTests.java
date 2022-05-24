package sxx.xwl.community.community;

import java.io.IOException;

/**
 * @author sxx_27
 * @create 2022-05-24 15:39
 */
public class WkTests {
    public static void main(String[] args) {
         String cmd = "d:/work/wkhtmltopdf/bin/wkhtmltoimage --quality 75 www.baidu.com d:/work/wk_img/3.png";

        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
