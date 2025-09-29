package com.fizzed.jne;

import com.fizzed.jne.internal.EtcPasswd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserEnvironmentDemo {
    static private final Logger log = LoggerFactory.getLogger(UserEnvironmentDemo.class);

    static public void main(String[] args) throws Exception {

        UserEnvironment userEnvironment = UserEnvironment.detectLogical();

        log.info("user: {}", userEnvironment.getUser());
        log.info("elevated: {}", userEnvironment.getElevated());
        log.info("homeDir: {}", userEnvironment.getHomeDir());
        log.info("shellType: {}", userEnvironment.getShellType());

        /*EtcPasswd etcPasswd = EtcPasswd.detect();

        etcPasswd.getEntries().forEach(entry -> {
            log.info("{}, home={}, shell={}", entry.getUsername(), entry.getHome(), entry.getShell());
        });*/

    }

}
