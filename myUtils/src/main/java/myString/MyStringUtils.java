package myString;

import mydate.MyDateUtil;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class MyStringUtils {
    public static final char UNDERLINE = '_';


    /**
     * @return String 获取一个字符，日期+随即字符串
     */
    public static String getIdDateStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(MyDateUtil.getDateStr("yyyyMMddHHmmss"));
        sb.append("-");
        sb.append(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10));
        return sb.toString();
    }

    /**
     * @return String 获取一个字符，日期+随即字符串
     */
    public static String getIdDateStr(String type) {
        StringBuffer sb = new StringBuffer();
        sb.append(MyDateUtil.getDateStr("yyyyMMddHHmmss"));
        sb.append("-");
        sb.append(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10));
        sb.append("_");
        sb.append(type);
        return sb.toString();
    }


    //驼峰转下划线
    public static String camelToUnderline(String param, Integer charType) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
            }
            if (charType == 2) {
                sb.append(Character.toUpperCase(c));  //统一都转大写
            } else {
                sb.append(Character.toLowerCase(c));  //统一都转小写
            }


        }
        return sb.toString();
    }

    //下划线转驼峰
    public static String underlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        Boolean flag = false; // "_" 后转大写标志,默认字符前面没有"_"
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE) {
                flag = true;
                continue;   //标志设置为true,跳过
            } else {
                if (flag == true) {
                    //表示当前字符前面是"_" ,当前字符转大写
                    sb.append(Character.toUpperCase(param.charAt(i)));
                    flag = false;  //重置标识
                } else {
                    sb.append(Character.toLowerCase(param.charAt(i)));
                }
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String s = DigestUtils.md5DigestAsHex("411521199902123456".substring(12,18).getBytes());
        System.out.println("411521199902123456".substring(12,18));
        System.out.println(s);
    }
}
