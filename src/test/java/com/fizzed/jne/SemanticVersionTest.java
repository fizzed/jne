package com.fizzed.jne;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

public class SemanticVersionTest {

    @Test
    void ubuntuLinuxVersion() {
        String s = "5.15.0-88-generic";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(5)));
        assertThat(v.getMinor(), is(equalTo(15)));
        assertThat(v.getPatch(), is(equalTo(0)));
        assertThat(v.getRevision(), is(equalTo(88))); // 88 is part of the flavor
        assertThat(v.getFlavor(), is(equalTo("generic")));
        assertThat(v.getBuildMetadata(), is(nullValue()));
        assertThat(v.getSource(), is(equalTo(s)));
    }

    @Test
    void macOSVersion() {
        String s = "23.1.0";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(23)));
        assertThat(v.getMinor(), is(equalTo(1)));
        assertThat(v.getPatch(), is(equalTo(0)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(nullValue()));
        assertThat(v.getBuildMetadata(), is(nullValue()));
    }

    @Test
    void freeBsdVersion() {
        String s = "13.2-RELEASE";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(13)));
        assertThat(v.getMinor(), is(equalTo(2)));
        assertThat(v.getPatch(), is(equalTo(0)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(equalTo("RELEASE")));
        assertThat(v.getBuildMetadata(), is(nullValue()));
    }

    @Test
    void openBsdVersion() {
        String s = "7.4";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(7)));
        assertThat(v.getMinor(), is(equalTo(4)));
        assertThat(v.getPatch(), is(equalTo(0)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(nullValue()));
    }

    @Test
    void netBsdVersion() {
        String s = "9.2_STABLE";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(9)));
        assertThat(v.getMinor(), is(equalTo(2)));
        assertThat(v.getPatch(), is(equalTo(0)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(equalTo("STABLE")));
    }

    @Test
    void dragonflyBsdVersion() {
        String s = "6.2.1-RELEASE";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(6)));
        assertThat(v.getMinor(), is(equalTo(2)));
        assertThat(v.getPatch(), is(equalTo(1)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(equalTo("RELEASE")));
    }

    @Test
    void solarisVersion() {
        String s = "5.11";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(5)));
        assertThat(v.getMinor(), is(equalTo(11)));
        assertThat(v.getPatch(), is(equalTo(0)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(nullValue()));
    }

    @Test
    void androidVersion() {
        String s = "4.19.113-gbe0c0b1122a2";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(4)));
        assertThat(v.getMinor(), is(equalTo(19)));
        assertThat(v.getPatch(), is(equalTo(113)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(equalTo("gbe0c0b1122a2")));
    }

    @Test
    void alpineLinuxVersion() {
        String s = "5.15.61-0-lts";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(5)));
        assertThat(v.getMinor(), is(equalTo(15)));
        assertThat(v.getPatch(), is(equalTo(61)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(equalTo("lts")));
    }

    @Test
    void java8Version() {
        String s = "1.8.0_311";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(1)));
        assertThat(v.getMinor(), is(equalTo(8)));
        assertThat(v.getPatch(), is(equalTo(0)));
        assertThat(v.getRevision(), is(equalTo(311)));
        assertThat(v.getFlavor(), is(nullValue()));
        assertThat(v.getBuildMetadata(), is(nullValue()));
    }

    @Test
    void java11Version() {
        String s = "11.0.1";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(11)));
        assertThat(v.getMinor(), is(equalTo(0)));
        assertThat(v.getPatch(), is(equalTo(1)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(nullValue()));
    }

    @Test
    void java17Version() {
        String s = "17.0.5+8-LTS-191";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(17)));
        assertThat(v.getMinor(), is(equalTo(0)));
        assertThat(v.getPatch(), is(equalTo(5)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(nullValue()));
        assertThat(v.getBuildMetadata(), is(equalTo("8-LTS-191")));
    }

    @Test
    void java21Version() {
        String s = "21.0.1+12-LTS";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(21)));
        assertThat(v.getMinor(), is(equalTo(0)));
        assertThat(v.getPatch(), is(equalTo(1)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(nullValue()));
        assertThat(v.getBuildMetadata(), is(equalTo("12-LTS")));
    }

    @Test
    void java23EarlyAccessVersion() {
        String s = "23-ea+20";
        SemanticVersion v = SemanticVersion.parse(s);

        assertThat(v.getMajor(), is(equalTo(23)));
        assertThat(v.getMinor(), is(equalTo(0)));
        assertThat(v.getPatch(), is(equalTo(0)));
        assertThat(v.getRevision(), is(equalTo(0)));
        assertThat(v.getFlavor(), is(equalTo("ea")));
        assertThat(v.getBuildMetadata(), is(equalTo("20")));
    }

    /*@Test
    void invalidInputs() {
        // This test uses JUnit 5's assertThrows, which is not AssertJ.
        // It's perfectly fine to leave as-is.
        assertThatThrownBy(() -> SemanticVersion.parse(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Version string cannot be null or empty.");

        assertThatThrownBy(() -> SemanticVersion.parse(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Version string cannot be null or empty.");

        assertThatThrownBy(() -> SemanticVersion.parse("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Version string cannot be null or empty.");
    }*/

    @Test
    void sortingLogic() {
        SemanticVersion v1 = SemanticVersion.parse("13.2-BETA");
        SemanticVersion v2 = SemanticVersion.parse("13.2");

        // A "final" version (v2) should be greater than a "beta" version (v1)
        assertThat(v1.compareTo(v2), is(lessThan(0)));
        assertThat(v2.compareTo(v1), is(greaterThan(0)));
    }

    @Test
    void equality() {
        SemanticVersion v1 = SemanticVersion.parse("17.0.5+8-LTS");
        SemanticVersion v2 = SemanticVersion.parse("17.0.5+12");

        // Build metadata should be ignored for equality
        assertThat(v1, is(equalTo(v2)));
        assertThat(v1.hashCode(), is(equalTo(v2.hashCode())));
    }
}