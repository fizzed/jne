package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2023 Fizzed, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

class JavaVersionTest {

    @Test
    public void invalid() {
        try {
            JavaVersion.parse(null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            JavaVersion.parse("");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            JavaVersion.parse("  ");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            JavaVersion.parse("blah");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void current() {
        JavaVersion v = JavaVersion.current();

        assertThat(v, is(not(nullValue())));
        assertThat(v.getMajor(), greaterThan(0));
    }

    @Test
    public void jsr223Version() {
        JavaVersion v;

        v = JavaVersion.parse("21");
        assertThat(v.getMajor(), is(21));
        assertThat(v.getMinor(), is(0));
        assertThat(v.getSecurity(), is(0));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("21.0.0"));

        v = JavaVersion.parse("21.");
        assertThat(v.getMajor(), is(21));
        assertThat(v.getMinor(), is(0));
        assertThat(v.getSecurity(), is(0));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("21.0.0"));

        v = JavaVersion.parse("21.2");
        assertThat(v.getMajor(), is(21));
        assertThat(v.getMinor(), is(2));
        assertThat(v.getSecurity(), is(0));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("21.2.0"));

        v = JavaVersion.parse("21.2.");
        assertThat(v.getMajor(), is(21));
        assertThat(v.getMinor(), is(2));
        assertThat(v.getSecurity(), is(0));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("21.2.0"));

        v = JavaVersion.parse("21.2.3");
        assertThat(v.getMajor(), is(21));
        assertThat(v.getMinor(), is(2));
        assertThat(v.getSecurity(), is(3));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("21.2.3"));

        v = JavaVersion.parse("21.2.3.");
        assertThat(v.getMajor(), is(21));
        assertThat(v.getMinor(), is(2));
        assertThat(v.getSecurity(), is(3));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("21.2.3"));

        v = JavaVersion.parse("21.2.3.1");
        assertThat(v.getMajor(), is(21));
        assertThat(v.getMinor(), is(2));
        assertThat(v.getSecurity(), is(3));
        assertThat(v.getBuild(), is(1));
        assertThat(v.toString(), is("21.2.3.1"));

        // another alternative we've seen too
        v = JavaVersion.parse("25.2.13+14");
        assertThat(v.getMajor(), is(25));
        assertThat(v.getMinor(), is(2));
        assertThat(v.getSecurity(), is(13));
        assertThat(v.getBuild(), is(14));
        assertThat(v.toString(), is("25.2.13.14"));
        assertThat(v.getSource(), is("25.2.13+14"));

        // another alternative we've seen too
        v = JavaVersion.parse("25+14");
        assertThat(v.getMajor(), is(25));
        assertThat(v.getMinor(), is(0));
        assertThat(v.getSecurity(), is(0));
        assertThat(v.getBuild(), is(14));
        assertThat(v.toString(), is("25.0.0.14"));

        // version on netbsd
        v = JavaVersion.parse(" 25-internal ");
        assertThat(v.getMajor(), is(25));
        assertThat(v.getMinor(), is(0));
        assertThat(v.getSecurity(), is(0));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("25.0.0"));
        assertThat(v.getSource(), is("25-internal"));
    }

    @Test
    public void legacyVersion() {
        JavaVersion v;

        v = JavaVersion.parse("1.8.0_392");
        assertThat(v.getMajor(), is(8));
        assertThat(v.getMinor(), is(0));
        assertThat(v.getSecurity(), is(392));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("8.0.392"));
        assertThat(v.toJavaVersion(), is("1.8.0_392"));

        v = JavaVersion.parse("1.8.5_392");
        assertThat(v.getMajor(), is(8));
        assertThat(v.getMinor(), is(5));
        assertThat(v.getSecurity(), is(392));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("8.5.392"));
        assertThat(v.toJavaVersion(), is("1.8.5_392"));

        v = JavaVersion.parse("1.7.0_45");
        assertThat(v.getMajor(), is(7));
        assertThat(v.getMinor(), is(0));
        assertThat(v.getSecurity(), is(45));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("7.0.45"));
        assertThat(v.toJavaVersion(), is("1.7.0_45"));

        v = JavaVersion.parse("1.6.0-119");
        assertThat(v.getMajor(), is(6));
        assertThat(v.getMinor(), is(0));
        assertThat(v.getSecurity(), is(119));
        assertThat(v.getBuild(), is(0));
        assertThat(v.toString(), is("6.0.119"));
        assertThat(v.toJavaVersion(), is("1.6.0_119"));
    }

    @Test
    public void compareTo() {
        JavaVersion v1;
        JavaVersion v2;

        v1 = JavaVersion.parse("22");
        v2 = JavaVersion.parse("21");
        assertThat(v1, greaterThan(v2));

        v1 = JavaVersion.parse("21.0.1");
        v2 = JavaVersion.parse("21");
        assertThat(v1, greaterThan(v2));

        v2 = JavaVersion.parse("21.0.1");
        v1 = JavaVersion.parse("17");
        assertThat(v1, lessThan(v2));

        v1 = JavaVersion.parse("17.0.0.0");
        v2 = JavaVersion.parse("17");
        assertThat(v1.equals(v2), is(true));

        TreeSet<JavaVersion> versionMap = new TreeSet<>();
        versionMap.add(JavaVersion.parse("11.0.9"));
        versionMap.add(JavaVersion.parse("11.0.7"));
        versionMap.add(JavaVersion.parse("1.8.0_349"));
        versionMap.add(JavaVersion.parse("11.0.345"));

        Iterator<JavaVersion> it = versionMap.iterator();
        assertThat(it.next(), is(JavaVersion.parse("1.8.0_349")));
        assertThat(it.next(), is(JavaVersion.parse("11.0.7")));
        assertThat(it.next(), is(JavaVersion.parse("11.0.9")));
        assertThat(it.next(), is(JavaVersion.parse("11.0.345")));
    }

}