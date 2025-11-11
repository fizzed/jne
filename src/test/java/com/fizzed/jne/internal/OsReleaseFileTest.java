package com.fizzed.jne.internal;

import com.fizzed.crux.util.Resources;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class OsReleaseFileTest {

    @Test
    void parseUbuntu2510() throws Exception {
        Path file = Resources.file("/com/fizzed/jne/internal/OsRelease-Ubuntu25.10.txt");

        OsReleaseFile osReleaseFile = OsReleaseFile.parse(file);

        assertThat(osReleaseFile.getId(), is("ubuntu"));
        assertThat(osReleaseFile.getVersion(), is("25.10 (Questing Quokka)"));
        assertThat(osReleaseFile.getName(), is("Ubuntu"));
        assertThat(osReleaseFile.getPrettyName(), is("Ubuntu 25.10"));
    }

    @Test
    void parseFedora43() throws Exception {
        Path file = Resources.file("/com/fizzed/jne/internal/OsRelease-Fedora43.txt");

        OsReleaseFile osReleaseFile = OsReleaseFile.parse(file);

        assertThat(osReleaseFile.getId(), is("fedora"));
        assertThat(osReleaseFile.getVersion(), is("43 (Workstation Edition)"));
        assertThat(osReleaseFile.getName(), is("Fedora Linux"));
        assertThat(osReleaseFile.getPrettyName(), is("Fedora Linux 43 (Workstation Edition)"));
    }

    @Test
    void parseFreeBSD13() throws Exception {
        Path file = Resources.file("/com/fizzed/jne/internal/OsRelease-FreeBSD13.txt");

        OsReleaseFile osReleaseFile = OsReleaseFile.parse(file);

        assertThat(osReleaseFile.getId(), is("freebsd"));
        assertThat(osReleaseFile.getVersion(), is("13.5-RELEASE-p6"));
        assertThat(osReleaseFile.getName(), is("FreeBSD"));
        assertThat(osReleaseFile.getPrettyName(), is("FreeBSD 13.5-RELEASE-p6"));
    }

}