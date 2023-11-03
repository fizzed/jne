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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
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

        try {
            JavaVersion.parse("1.B");
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
    }

}
