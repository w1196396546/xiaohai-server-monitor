package org.server.monitor;

import cn.hutool.setting.Setting;

public class ConfigUtil {

    public static Setting SETTING=new Setting("E:\\ideaProject\\springboot\\whq-server-monitor\\whq-server-monitor\\src\\main\\resources\\config.conf");

    private static String applicationName;

    public static String getApplicationName(){
        return SETTING.getStr("application.name");
    }
}
