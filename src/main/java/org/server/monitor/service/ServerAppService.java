package org.server.monitor.service;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.apache.commons.net.telnet.TelnetClient;
import org.server.monitor.domain.ServerApp;
import org.server.monitor.domain.User;
import org.server.monitor.utils.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class ServerAppService {
    private static final ExecutorService executor = ExecutorBuilder.create().setCorePoolSize(10).
            setMaxPoolSize(30).useSynchronousQueue().build();
    private List<ServerApp> serverApps=new ArrayList<>();
    private static final Log log = LogFactory.get();
    public void initApps(){
        log.info("----------初始化监控服务-------------");
        //获取需要监控的服务
        for (String groupName : ConfigUtil.SETTING.getGroups()) {
            //获取ip
            String ip = ConfigUtil.SETTING.getStr("ip", groupName, null);
            //获取端口号
            Integer port = ConfigUtil.SETTING.getInt("port", groupName);
            //IP跟端口不能为空，跳过
            if (ip==null || port==null)continue;
            //获取affects
            String affects = ConfigUtil.SETTING.getStr("affects", groupName, "[未说明]");
            //得到用户
            String users = ConfigUtil.SETTING.getStr("users", groupName, null);
            //得到分组(一个组多个用户)
            String groups = ConfigUtil.SETTING.getStr("groups", groupName, null);
            Set<User> set=new HashSet<>();
            if (users!=null && users.trim().length()>0){
                String[] userArr = users.split("\\.");
                List<User> usersByUserNames = UserUtils.getUsersByUserNames(userArr);
                set.addAll(usersByUserNames);
            }
            if (groups!=null && groups.trim().length()>0){
                String[] groupArr = groups.split("\\.");
                LinkedHashSet<User> usersByGroups = GroupUtil.getUsersByGroups(groupArr);
                set.addAll(usersByGroups);
            }
            ServerApp serverApp=new ServerApp();
            serverApp.setIp(ip);
            serverApp.setPort(port);
            serverApp.setAffects(affects);
            serverApp.setName(groupName);
            if (set.size()>0)serverApp.setUsers(new ArrayList<>(set));
            serverApps.add(serverApp);
            log.info(String.format("%s\t\t%s:%s", groupName, ip, port+""));
        }
        log.info("------------------------------------");
    }

    public void checkServerApps(){
        for (ServerApp app : this.serverApps) {
            TelnetClient telnetClient = TelnetUtil.getTelnetClient();
            try {
                //超时时间
                telnetClient.setConnectTimeout(5000);
                //开始连接
                telnetClient.connect(app.getIp(), app.getPort());
                //断开连接
                telnetClient.disconnect();
                log.info("{}:服务正常 [{}:{}]", app.getName(), app.getIp(), app.getPort());
                if (app.getDownDate()!=null){
                    //服务挂掉的时间不为空的话，证明之前挂了，走恢复服务通知
                    restore(app);
                }
            } catch (IOException e) {
                log.info("{}:服务异常 [{}:{}]", app.getName(), app.getIp(), app.getPort());
                if (app.getDownDate()==null){
                    //第一次异常
                    anomaly(app);
                }else {
                    //重复异常
                    int minute = (int)(new Date().getTime() - app.getDownDate().getTime()) / 1000 / 60;
                    if (minute > 0 && minute % 5 == 0 && !Objects.equals(minute, app.getLastNotifyMinutesFromDownTime())) {
                        app.setLastNotifyMinutesFromDownTime(minute);
                        anomalyAgain(app, minute);
                    }
                }
            }
        }
    }

    private void anomaly(ServerApp app) {
        if (app.getDownDate() != null) {
            return;
        }
        Date timeNow = new Date();
        String timeNowStr = DateUtil.formatDateTime(timeNow);

        String title =  String.format("%s%s已停止服务！请注意检查！", ConfigUtil.getApplicationName2(), app.getName());
        String content = String.format("时间：%s\n服务：%s\nIP：%s\n端口：%s\n影响服务：%s", timeNowStr , app.getName(), app.getIp(), app.getPort(), app.getAffects());


        startNotify(app, timeNowStr, title, content, "offline");

        app.setDownDate(new Date());
    }
    public void anomalyAgain(ServerApp app, Integer minutes) {
        if (app.getDownDate() == null) {
            return;
        }
        Date timeNow = new Date();
        String timeNowStr = DateUtil.formatDateTime(timeNow);
        String downTimeStr = DateUtil.formatDateTime(app.getDownDate());

        String title =  String.format("%s%s已停止服务超过%s分钟了！请注意检查！", ConfigUtil.getApplicationName2(), app.getName(), minutes+"");
        String content = String.format("停止时间：%s\n服务：%s\nIP：%s\n端口：%s\n影响服务：%s", downTimeStr , app.getName(), app.getIp(), app.getPort(), app.getAffects());

        startNotify(app, timeNowStr, title, content, "offline");
    }
    public void restore(ServerApp app) {
        if (app.getDownDate() == null) {
            return;
        }
        long between =  new Date().getTime() - app.getDownDate().getTime();
        String formatBetween = DateUtil.formatBetween(between, BetweenFormatter.Level.SECOND);

        Date timeNow = new Date();
        String timeNowStr = DateUtil.formatDateTime(timeNow);

        String title =  String.format("%s%s已恢复服务！本次累计停止服务%s", ConfigUtil.getApplicationName2(), app.getName(), formatBetween);
        String content = String.format("时间：%s\n服务：%s\nIP：%s\n端口：%s\n累计停止时间：%s\n影响服务：%s", timeNowStr, app.getName(), app.getIp(), app.getPort(), formatBetween, app.getAffects());

        startNotify(app, timeNowStr, title, content, "online");

        app.setDownDate(null);
//        app.setLastNotifyMinutesFromDownTime(null);
    }

    private void startNotify(ServerApp app, String timeNowStr, String title, String content, String online) {
        executor.submit(()->{
            try {
                log.info("发送邮件：\n{}\n{}\n--------------------------", title, content);
                List<String> userMails = app.getUsers().stream().filter(u -> StrUtil.isNotBlank(u.getMail())).map(User::getMail).collect(Collectors.toList());
                for (String userMail : userMails) {
                    System.out.println("userMail:"+userMail);
                }
                MailUtil.sendEMail(title, content, userMails, false);
            } catch (Exception e) {
                log.error("发送邮件失败", e);
            }
        });
    }
}
