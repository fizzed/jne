package com.fizzed.jne.internal;

import com.fizzed.crux.util.Resources;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class EtcPasswdTest {

    @Test
    public void parse() throws Exception {
        Path file = Resources.file("/com/fizzed/jne/internal/EtcPasswdContainer.txt");

        EtcPasswd etcPasswd = EtcPasswd.parse(file);

        final EtcPasswd.Entry root = etcPasswd.findEntryByUserName("root");

        assertThat(root, is(not(nullValue())));
        assertThat(root.getShell(), is("/bin/bash"));
    }

}