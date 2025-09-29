package com.fizzed.jne;

import com.fizzed.jne.internal.EtcPasswd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserEnvironmentDemo {
    static private final Logger log = LoggerFactory.getLogger(UserEnvironmentDemo.class);

    static public void main(String[] args) throws Exception {

        EtcPasswd etcPasswd = EtcPasswd.detect();

        etcPasswd.getEntries().forEach(entry -> {
            log.info("{}, home={}, shell={}", entry.getUsername(), entry.getHome(), entry.getShell());
        });

    }

}
