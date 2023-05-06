package org.server.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerApp {
    private String name;
    private String ip;
    private Integer port;
    private String affects;
    private List<User> users;
    private Date downDate;
    private Integer lastNotifyMinutesFromDownTime;
}
