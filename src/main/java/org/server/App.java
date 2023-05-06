package org.server;

import cn.hutool.log.LogFactory;
import cn.hutool.log.dialect.console.ConsoleLogFactory;
import org.server.monitor.domain.ServerApp;
import org.server.monitor.service.ServerAppService;
import org.server.monitor.utils.ConfigUtil;
import org.server.monitor.utils.GroupUtil;
import org.server.monitor.utils.UserUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        LogFactory.setCurrentLogFactory(new ConsoleLogFactory());
        UserUtils.loadUsers();
        GroupUtil.loadGroup();
        ServerAppService service=new ServerAppService();
        service.initApps();
        System.out.println("------------开始监控---------------");
        while (true){
            service.checkServerApps();
            System.out.println();
            System.out.println();

            try {
                Thread.sleep(3000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void test(){

    }
}
