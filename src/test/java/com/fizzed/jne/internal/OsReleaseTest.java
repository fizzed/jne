package com.fizzed.jne.internal;

import com.fizzed.crux.util.Resources;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OsReleaseTest {

    @Test
    void parseUbuntu2510() throws Exception {
        Path file = Resources.file("/com/fizzed/jne/internal/OsRelease-Ubuntu25.10.txt");

        OsRelease osRelease = OsRelease.parse(file);

        assertThat(osRelease.getId(), is("ubuntu"));
        assertThat(osRelease.getVersion(), is("25.10 (Questing Quokka)"));
        assertThat(osRelease.getName(), is("Ubuntu"));
        assertThat(osRelease.getPrettyName(), is("Ubuntu 25.10"));
    }

    @Test
    void parseFedora43() throws Exception {
        Path file = Resources.file("/com/fizzed/jne/internal/OsRelease-Fedora43.txt");

        OsRelease osRelease = OsRelease.parse(file);

        assertThat(osRelease.getId(), is("fedora"));
        assertThat(osRelease.getVersion(), is("43 (Workstation Edition)"));
        assertThat(osRelease.getName(), is("Fedora Linux"));
        assertThat(osRelease.getPrettyName(), is("Fedora Linux 43 (Workstation Edition)"));
    }

    @Test
    void parseFreeBSD13() throws Exception {
        Path file = Resources.file("/com/fizzed/jne/internal/OsRelease-FreeBSD13.txt");

        OsRelease osRelease = OsRelease.parse(file);

        assertThat(osRelease.getId(), is("freebsd"));
        assertThat(osRelease.getVersion(), is("13.5-RELEASE-p6"));
        assertThat(osRelease.getName(), is("FreeBSD"));
        assertThat(osRelease.getPrettyName(), is("FreeBSD 13.5-RELEASE-p6"));
    }

}