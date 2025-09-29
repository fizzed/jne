package com.fizzed.jne;

import com.fizzed.jne.internal.EtcPasswd;
import com.fizzed.jne.internal.MacDscl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class UserEnvironmentDemo {
    static private final Logger log = LoggerFactory.getLogger(UserEnvironmentDemo.class);

    static public void main(String[] args) throws Exception {

        //MacDscl.read(Paths.get("/Users/jjlauer"));

//        UserEnvironment userEnvironment = UserEnvironment.detectLogical();
        UserEnvironment userEnvironment = UserEnvironment.detectEffective();

        log.info("user: {}", userEnvironment.getUser());
        log.info("elevated: {}", userEnvironment.getElevated());
        log.info("displayName: {}", userEnvironment.getDisplayName());
        log.info("userId: {}", userEnvironment.getUserId());
        log.info("groupId: {}", userEnvironment.getGroupId());
        log.info("homeDir: {}", userEnvironment.getHomeDir());
        log.info("shell: {}", userEnvironment.getShell());
        log.info("shellType: {}", userEnvironment.getShellType());

        /*EtcPasswd etcPasswd = EtcPasswd.detect();

        etcPasswd.getEntries().forEach(entry -> {
            log.info("{}, home={}, shell={}", entry.getUsername(), entry.getHome(), entry.getShell());
        });*/

    }

}
