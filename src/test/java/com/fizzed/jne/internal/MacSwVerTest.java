package com.fizzed.jne.internal;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MacMacSwVerTest {

    @Test
    void macOS_10_10_Yosemite() {
        String output = "ProductName:    Mac OS X\n" +
            "ProductVersion: 10.10.5\n" +
            "BuildVersion:   14F27";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("Mac OS X")));
        assertThat(v.getProductVersion(), is(equalTo("10.10.5")));
        assertThat(v.getBuildVersion(), is(equalTo("14F27")));
    }

    @Test
    void macOS_10_11_ElCapitan() {
        // Note: ProductName changed to "OS X"
        String output = "ProductName:    OS X\n" +
            "ProductVersion: 10.11.6\n" +
            "BuildVersion:   15G31";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("OS X")));
        assertThat(v.getProductVersion(), is(equalTo("10.11.6")));
        assertThat(v.getBuildVersion(), is(equalTo("15G31")));
    }

    @Test
    void macOS_10_12_Sierra() {
        // Note: ProductName changed to "macOS"
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 10.12.6\n" +
            "BuildVersion:   16G29";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("10.12.6")));
        assertThat(v.getBuildVersion(), is(equalTo("16G29")));
    }

    @Test
    void macOS_10_13_HighSierra() {
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 10.13.6\n" +
            "BuildVersion:   17G65";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("10.13.6")));
        assertThat(v.getBuildVersion(), is(equalTo("17G65")));
    }

    @Test
    void macOS_10_14_Mojave() {
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 10.14.6\n" +
            "BuildVersion:   18G84";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("10.14.6")));
        assertThat(v.getBuildVersion(), is(equalTo("18G84")));
    }

    @Test
    void macOS_10_15_Catalina() {
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 10.15.7\n" +
            "BuildVersion:   19H15";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("10.15.7")));
        assertThat(v.getBuildVersion(), is(equalTo("19H15")));
    }

    @Test
    void macOS_11_BigSur() {
        // Note: Major version number changed
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 11.7.10\n" +
            "BuildVersion:   20G1427";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("11.7.10")));
        assertThat(v.getBuildVersion(), is(equalTo("20G1427")));
    }

    @Test
    void macOS_12_Monterey() {
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 12.7.1\n" +
            "BuildVersion:   21G920";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("12.7.1")));
        assertThat(v.getBuildVersion(), is(equalTo("21G920")));
    }

    @Test
    void macOS_13_Ventura() {
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 13.6.1\n" +
            "BuildVersion:   22G313";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("13.6.1")));
        assertThat(v.getBuildVersion(), is(equalTo("22G313")));
    }

    @Test
    void macOS_14_Sonoma() {
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 14.1.1\n" +
            "BuildVersion:   23B81";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("14.1.1")));
        assertThat(v.getBuildVersion(), is(equalTo("23B81")));
    }

    @Test
    void macOS_15_Sequoia() {
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 15.0\n" +
            "BuildVersion:   24A326";
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("15.0")));
        assertThat(v.getBuildVersion(), is(equalTo("24A326")));
    }

    @Test
    void macOS_26_Tahoe() {
        // Extrapolating based on current (2025) naming conventions
        String output = "ProductName:    macOS\n" +
            "ProductVersion: 26.0\n" +
            "BuildVersion:   25A123\n\n"; // Example build
        MacSwVer v = MacSwVer.parse(output);
        assertThat(v.getProductName(), is(equalTo("macOS")));
        assertThat(v.getProductVersion(), is(equalTo("26.0")));
        assertThat(v.getBuildVersion(), is(equalTo("25A123")));
    }

}