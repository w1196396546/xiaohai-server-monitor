package org.server.monitor.utils;

import org.server.monitor.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserUtils {

    public static Map<String, User> allUsers=new HashMap<>(16);

    public static void loadUsers(){
        List<String> userKeys = ConfigUtil.SETTING.keySet().stream().filter(k -> k.matches("^users\\.[^.]*\\..*$")).sorted().collect(Collectors.toList());
        allUsers = new HashMap<>();
        if (userKeys.size()==0){
            return;
        }
        for (String userKey : userKeys) {
            String[] keyArr = userKey.split("\\.");
            if (keyArr.length!=3){
                continue;
            }
            String userName=keyArr[1];
            String propName=keyArr[2];
            String value = ConfigUtil.SETTING.getStr(userKey);
            User user = allUsers.get(userName);
            if (user==null){
                user=new User();
                user.setName(userName);
                allUsers.put(userName,user);
            }
            if (propName.equals("mail")){
                //email消息
                user.setMail(value);
                System.out.println("user:"+user);
            }else if (propName.equals("")){
                //实现其他的通知方式
            }
        }
    }
    public static User getUserByUserName(String userName) {
        return allUsers.get(userName);
    }
    public static List<User> getUsersByUserNames(String...UserNames) {
        List<User> result = new ArrayList<>();
        for (String key : UserNames) {
            result.add(allUsers.get(key));
        }
        return result;
    }
}
