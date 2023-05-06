package org.server.monitor.utils;

import org.server.monitor.domain.Group;
import org.server.monitor.domain.User;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取组
 */
public class GroupUtil {
    public static Map<String, Group> groupMap=new HashMap<>();

    public static void loadGroup(){
        groupMap=new HashMap<>();
        List<String> groupKeys = ConfigUtil.SETTING.keySet().stream().filter(k -> k.matches("^groups\\.[^.]*$")).sorted().collect(Collectors.toList());
        for (String groupKey : groupKeys) {
            String[] keys = groupKey.split("\\.");
            if (keys.length != 2){
                System.out.println("跳了吗");
                continue;
            }
            //group名称
            String groupName=keys[1];
            System.out.println("groupName:"+groupName);
            String value = ConfigUtil.SETTING.getStr(groupKey);
            if (value==null||value.length()==0){
                continue;
            }
            //分割出用户名称
            String[] names = value.split("\\.");
            if (names==null)continue;
            Group group=new Group();
            group.setGroupName(groupName);
            for (String name : names) {
                //过滤重复的用户
                if (group.getUsers().stream().map(User::getName).collect(Collectors.toList()).contains(name)){
                    continue;
                }
                //不重复。得到用户信息
                User userByUserName = UserUtils.getUserByUserName(name);
                if (userByUserName==null) continue;
                group.getUsers().add(userByUserName);
            }
            groupMap.put(groupName,group);
        }
    }
    public static LinkedHashSet<User> getUsersByGroups(String...groupNames) {
        LinkedHashSet<User> result = new LinkedHashSet<>();
        if (groupNames == null || groupNames.length == 0) {
            return result;
        }
        for (String groupName : groupNames) {
            Group group = groupMap.get(groupName);
            if (group == null) {
                continue;
            }
            List<User> users = group.getUsers();
            if (users == null) {
                continue;
            }
            result.addAll(users);
        }

        return result;

    }
}
