package com.fizzed.jne.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit 5 test class for the Uname parser.
 * This tests the 10 example strings from the Uname.main() method.
 */
public class UnameTest {

    @Test
    public void parseLinux() {
        String linuxOutput = "Linux my-ubuntu-vm 5.15.0-88-generic #98-Ubuntu SMP Mon Oct 2 15:18:56 UTC 2023 x86_64 x86_64 x86_64 GNU/Linux";
        Uname u = Uname.parse(linuxOutput);

        assertNotNull(u);
        assertEquals("Linux", u.getSysname());
        assertEquals("my-ubuntu-vm", u.getNodename());
        assertEquals("5.15.0-88-generic", u.getVersion());
        assertEquals("#98-Ubuntu SMP Mon Oct 2 15:18:56 UTC 2023", u.getFlavor());
        assertEquals("x86_64", u.getMachine());

        // GNU-specific fields
        assertNotNull(u.getProcessor());
        assertNotNull(u.getHardwarePlatform());
        assertNotNull(u.getOperatingSystem());

        assertEquals("x86_64", u.getProcessor());
        assertEquals("x86_64", u.getHardwarePlatform());
        assertEquals("GNU/Linux", u.getOperatingSystem());
        assertEquals(linuxOutput, u.getSource());
    }

    @Test
    public void parseMacos() {
        String macosOutput = "Darwin My-MacBook.local 23.1.0 Darwin Kernel Version 23.1.0: Mon Oct 9 21:27:24 PDT 2023; root:xnu-10002.41.9~6/RELEASE_X86_64 x86_64";
        Uname u = Uname.parse(macosOutput);

        assertNotNull(u);
        assertEquals("Darwin", u.getSysname());
        assertEquals("My-MacBook.local", u.getNodename());
        assertEquals("23.1.0", u.getVersion());
        assertEquals("Darwin Kernel Version 23.1.0: Mon Oct 9 21:27:24 PDT 2023; root:xnu-10002.41.9~6/RELEASE_X86_64", u.getFlavor());
        assertEquals("x86_64", u.getMachine());

        // GNU-specific fields should be null
        assertNull(u.getProcessor());
        assertNull(u.getHardwarePlatform());
        assertNull(u.getOperatingSystem());
    }

    @Test
    public void parseFreeBSD() {
        String freebsdOutput = "FreeBSD my-bsd-host 13.2-RELEASE FreeBSD 13.2-RELEASE #0 releng/13.2-n254617-525ecfd85554: Mon Apr 10 05:54:13 UTC 2023 root@releng1.FreeBSD.org:/usr/obj/usr/src/amd64.amd64/sys/GENERIC amd64";
        Uname u = Uname.parse(freebsdOutput);

        assertNotNull(u);
        assertEquals("FreeBSD", u.getSysname());
        assertEquals("my-bsd-host", u.getNodename());
        assertEquals("13.2-RELEASE", u.getVersion());
        assertEquals("FreeBSD 13.2-RELEASE #0 releng/13.2-n254617-525ecfd85554: Mon Apr 10 05:54:13 UTC 2023 root@releng1.FreeBSD.org:/usr/obj/usr/src/amd64.amd64/sys/GENERIC", u.getFlavor());
        assertEquals("amd64", u.getMachine());

        // GNU-specific fields should be null
        assertNull(u.getProcessor());
        assertNull(u.getHardwarePlatform());
        assertNull(u.getOperatingSystem());
    }

    @Test
    public void parseOpenBSD() {
        String openbsdOutput = "OpenBSD my-openbsd.local 7.4 GENERIC.MP#6 amd64";
        Uname u = Uname.parse(openbsdOutput);

        assertNotNull(u);
        assertEquals("OpenBSD", u.getSysname());
        assertEquals("my-openbsd.local", u.getNodename());
        assertEquals("7.4", u.getVersion());
        assertEquals("GENERIC.MP#6", u.getFlavor());
        assertEquals("amd64", u.getMachine());

        // GNU-specific fields should be null
        assertNull(u.getProcessor());
        assertNull(u.getHardwarePlatform());
        assertNull(u.getOperatingSystem());
    }

    @Test
    public void parseNetBSD() {
        String netbsdOutput = "NetBSD my-netbsd.host 9.2_STABLE NetBSD 9.2_STABLE (GENERIC) #0: Wed Apr 15 12:34:56 UTC 2020 root@build.netbsd.org:/usr/obj/sys/arch/amd64/compile/GENERIC amd64";
        Uname u = Uname.parse(netbsdOutput);

        assertNotNull(u);
        assertEquals("NetBSD", u.getSysname());
        assertEquals("my-netbsd.host", u.getNodename());
        assertEquals("9.2_STABLE", u.getVersion());
        assertEquals("NetBSD 9.2_STABLE (GENERIC) #0: Wed Apr 15 12:34:56 UTC 2020 root@build.netbsd.org:/usr/obj/sys/arch/amd64/compile/GENERIC", u.getFlavor());
        assertEquals("amd64", u.getMachine());

        // GNU-specific fields should be null
        assertNull(u.getProcessor());
        assertNull(u.getHardwarePlatform());
        assertNull(u.getOperatingSystem());
    }

    @Test
    public void parseDragonFlyBSD() {
        String dragonflyOutput = "DragonFly my-dragonfly.host 6.2.1-RELEASE DragonFly v6.2.1-RELEASE #0: Sun Jul 10 14:48:58 PDT 2022 root@my-dragonfly.host:/usr/obj/kernels/X86_64/GENERIC x86_64";
        Uname u = Uname.parse(dragonflyOutput);

        assertNotNull(u);
        assertEquals("DragonFly", u.getSysname());
        assertEquals("my-dragonfly.host", u.getNodename());
        assertEquals("6.2.1-RELEASE", u.getVersion());
        assertEquals("DragonFly v6.2.1-RELEASE #0: Sun Jul 10 14:48:58 PDT 2022 root@my-dragonfly.host:/usr/obj/kernels/X86_64/GENERIC", u.getFlavor());
        assertEquals("x86_64", u.getMachine());

        // GNU-specific fields should be null
        assertNull(u.getProcessor());
        assertNull(u.getHardwarePlatform());
        assertNull(u.getOperatingSystem());
    }

    @Test
    public void parseSolaris() {
        String solarisOutput = "SunOS my-solaris-host 5.11 11.4.0.15.0 sun4v";
        Uname u = Uname.parse(solarisOutput);

        assertNotNull(u);
        assertEquals("SunOS", u.getSysname());
        assertEquals("my-solaris-host", u.getNodename());
        assertEquals("5.11", u.getVersion());
        assertEquals("11.4.0.15.0", u.getFlavor());
        assertEquals("sun4v", u.getMachine());

        // GNU-specific fields should be null
        assertNull(u.getProcessor());
        assertNull(u.getHardwarePlatform());
        assertNull(u.getOperatingSystem());
    }

    @Test
    public void parseOpenIndiana() {
        String openIndianaOutput = "SunOS openindiana 5.11 illumos-f30752b04c i86pc";
        Uname u = Uname.parse(openIndianaOutput);

        assertNotNull(u);
        assertEquals("SunOS", u.getSysname());
        assertEquals("openindiana", u.getNodename());
        assertEquals("5.11", u.getVersion());
        assertEquals("illumos-f30752b04c", u.getFlavor());
        assertEquals("i86pc", u.getMachine());

        // GNU-specific fields should be null
        assertNull(u.getProcessor());
        assertNull(u.getHardwarePlatform());
        assertNull(u.getOperatingSystem());
    }

    @Test
    public void parseAndroid() {
        String androidOutput = "Linux localhost 4.19.113-gbe0c0b1122a2 #1 SMP PREEMPT Tue Jun 22 17:09:44 UTC 2021 aarch64";
        Uname u = Uname.parse(androidOutput);

        assertNotNull(u);
        assertEquals("Linux", u.getSysname());
        assertEquals("localhost", u.getNodename());
        assertEquals("4.19.113-gbe0c0b1122a2", u.getVersion());
        assertEquals("#1 SMP PREEMPT Tue Jun 22 17:09:44 UTC 2021", u.getFlavor());
        assertEquals("aarch64", u.getMachine());

        // GNU-specific fields should be null
        assertNull(u.getProcessor());
        assertNull(u.getHardwarePlatform());
        assertNull(u.getOperatingSystem());
    }

    @Test
    public void parseAlpineLinux() {
        String alpineOutput = "Linux my-alpine-host 5.15.61-0-lts #1-Alpine SMP Thu Aug 18 12:53:14 UTC 2022 x86_64";
        Uname u = Uname.parse(alpineOutput);

        assertNotNull(u);
        assertEquals("Linux", u.getSysname());
        assertEquals("my-alpine-host", u.getNodename());
        assertEquals("5.15.61-0-lts", u.getVersion());
        assertEquals("#1-Alpine SMP Thu Aug 18 12:53:14 UTC 2022", u.getFlavor());
        assertEquals("x86_64", u.getMachine());

        // GNU-specific fields should be null
        assertNull(u.getProcessor());
        assertNull(u.getHardwarePlatform());
        assertNull(u.getOperatingSystem());
    }

    @Test
    public void invalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> {
            Uname.parse(null);
        }, "Parsing null should throw exception");

        assertThrows(IllegalArgumentException.class, () -> {
            Uname.parse("");
        }, "Parsing empty string should throw exception");

        assertThrows(IllegalArgumentException.class, () -> {
            Uname.parse("   ");
        }, "Parsing blank string should throw exception");

        assertThrows(IllegalArgumentException.class, () -> {
            Uname.parse("one two three four");
        }, "Parsing too few fields should throw exception");
    }
}