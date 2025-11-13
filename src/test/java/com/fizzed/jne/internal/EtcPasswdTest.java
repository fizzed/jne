package com.fizzed.jne.internal;

import com.fizzed.crux.util.Resources;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class EtcPasswdTest {

    @Test
    public void ubuntu2510() throws Exception {
        Path file = Resources.file("/fixtures/platforms/ubuntu2510/cat-etcpasswd.txt");

        EtcPasswd etcPasswd = EtcPasswd.parse(file);

        final EtcPasswd.Entry user = etcPasswd.findEntryByUserName("root");

        assertThat(user, is(not(nullValue())));
        assertThat(user.getShell(), is("/bin/bash"));
        assertThat(user.getHome(), is("/root"));
        assertThat(user.getUsername(), is("root"));
        assertThat(user.getName(), is("root"));
        assertThat(user.getUserId(), is(0));
        assertThat(user.getGroupId(), is(0));
    }

    @Test
    public void macos11() throws Exception {
        Path file = Resources.file("/fixtures/platforms/macos11/cat-etcpasswd.txt");

        EtcPasswd etcPasswd = EtcPasswd.parse(file);

        final EtcPasswd.Entry user = etcPasswd.findEntryByUserName("root");

        assertThat(user, is(not(nullValue())));
        assertThat(user.getShell(), is("/bin/sh"));
        assertThat(user.getHome(), is("/var/root"));
        assertThat(user.getUsername(), is("root"));
        assertThat(user.getName(), is("System Administrator"));
        assertThat(user.getUserId(), is(0));
        assertThat(user.getGroupId(), is(0));
    }

    @Test
    public void freebsd13() throws Exception {
        Path file = Resources.file("/fixtures/platforms/freebsd13/cat-etcpasswd.txt");

        EtcPasswd etcPasswd = EtcPasswd.parse(file);

        final EtcPasswd.Entry user = etcPasswd.findEntryByUserName("builder");

        assertThat(user, is(not(nullValue())));
        assertThat(user.getShell(), is("/bin/csh"));
        assertThat(user.getHome(), is("/home/builder"));
        assertThat(user.getUsername(), is("builder"));
        assertThat(user.getName(), is("builder"));
        assertThat(user.getUserId(), is(1001));
        assertThat(user.getGroupId(), is(1001));
    }

}