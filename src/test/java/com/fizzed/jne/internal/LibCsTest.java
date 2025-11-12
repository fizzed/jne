package com.fizzed.jne.internal;

import com.fizzed.crux.util.Resources;
import com.fizzed.jne.LibC;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class LibCsTest {

    @Test
    public void pathUbuntu1604() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/ubuntu1604/exec-ldd-binls.txt");

        final LibCs.PathResult result = LibCs.parsePath(content);

        assertThat(result.getLibC(), is(LibC.GLIBC));
        assertThat(result.getPath(), is("/lib/x86_64-linux-gnu/libc.so.6"));
    }

    @Test
    public void versionUbuntu1604() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/ubuntu1604/exec-libx8664linuxgnulibcso6.txt");

        final String version = LibCs.parseVersion(content);

        assertThat(version, is("2.23"));
    }

    @Test
    public void pathUbuntu1804() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/ubuntu1804/exec-ldd-binls.txt");

        final LibCs.PathResult result = LibCs.parsePath(content);

        assertThat(result.getLibC(), is(LibC.GLIBC));
        assertThat(result.getPath(), is("/lib/x86_64-linux-gnu/libc.so.6"));
    }

    @Test
    public void versionUbuntu1804() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/ubuntu1804/exec-libx8664linuxgnulibcso6.txt");

        final String version = LibCs.parseVersion(content);

        assertThat(version, is("2.27"));
    }

    @Test
    public void pathUbuntu2510() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/ubuntu2510/exec-ldd-binls.txt");

        final LibCs.PathResult result = LibCs.parsePath(content);

        assertThat(result.getLibC(), is(LibC.GLIBC));
        assertThat(result.getPath(), is("/lib/x86_64-linux-gnu/libc.so.6"));
    }

    @Test
    public void versionUbuntu2510() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/ubuntu2510/exec-libx8664linuxgnulibcso6.txt");

        final String version = LibCs.parseVersion(content);

        assertThat(version, is("2.42"));
    }

    @Test
    public void pathFedora43() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/fedora43/exec-ldd-binls.txt");

        final LibCs.PathResult result = LibCs.parsePath(content);

        assertThat(result.getLibC(), is(LibC.GLIBC));
        assertThat(result.getPath(), is("/lib64/libc.so.6"));
    }

    @Test
    public void versionFedora43() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/fedora43/exec-lib64libcso6.txt");

        final String version = LibCs.parseVersion(content);

        assertThat(version, is("2.42"));
    }

    @Test
    public void pathAlpine315() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/alpine315/exec-ldd-binls.txt");

        final LibCs.PathResult result = LibCs.parsePath(content);

        assertThat(result.getLibC(), is(LibC.MUSL));
        assertThat(result.getPath(), is("/lib/ld-musl-x86_64.so.1"));
    }

    @Test
    public void versionAlpine315() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/alpine315/exec-libldmuslx8664so1.txt");

        final String version = LibCs.parseVersion(content);

        assertThat(version, is("1.2.2"));
    }

    @Test
    public void pathVoidLinux() throws Exception {
        String content = Resources.stringUTF8("/fixtures/platforms/voidlinux/exec-ldd-binls.txt");

        final LibCs.PathResult result = LibCs.parsePath(content);

        assertThat(result.getLibC(), is(LibC.MUSL));
        assertThat(result.getPath(), is("/lib/ld-musl-x86_64.so.1"));
    }

}