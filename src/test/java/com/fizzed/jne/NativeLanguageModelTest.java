package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2024 Fizzed, Inc
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class NativeLanguageModelTest {

    @Test
    public void os() {
        NativeLanguageModel model = new NativeLanguageModel();
        model.add(OperatingSystem.WINDOWS, "win");

        assertThat(model.format(OperatingSystem.WINDOWS), is("win"));
        assertThat(model.format(OperatingSystem.LINUX), is("linux"));
    }

    @Test
    public void arch() {
        NativeLanguageModel model = new NativeLanguageModel();
        model.add(HardwareArchitecture.X64, "amd64");

        assertThat(model.format(HardwareArchitecture.X64), is("amd64"));
        assertThat(model.format(HardwareArchitecture.ARM64), is("arm64"));
    }

    @Test
    public void format() {
        NativeLanguageModel model = new NativeLanguageModel();
        model.add(OperatingSystem.WINDOWS, "win");
        model.add(HardwareArchitecture.X64, "amd64");
        model.add(HardwareArchitecture.ARM64, "aarch64");

        assertThat(model.format("v17.0.1-{os}-{arch}", OperatingSystem.WINDOWS, HardwareArchitecture.X64, null), is("v17.0.1-win-amd64"));
        assertThat(model.format("v17.0.1-{os}-{arch}", OperatingSystem.LINUX, HardwareArchitecture.X64, null), is("v17.0.1-linux-amd64"));
        assertThat(model.format("v17.0.1-{os}-{arch}", OperatingSystem.LINUX, HardwareArchitecture.ARM64, null), is("v17.0.1-linux-aarch64"));
        assertThat(model.format("v17.0.1-{os}-{arch}{_?abi}", OperatingSystem.LINUX, HardwareArchitecture.ARM64, null), is("v17.0.1-linux-aarch64"));
        assertThat(model.format("v17.0.1-{os}{_?abi}-{arch}", OperatingSystem.LINUX, HardwareArchitecture.ARM64, ABI.MUSL), is("v17.0.1-linux_musl-aarch64"));

        model.add("version", "17.0.1");

        assertThat(model.format("v{version}-{os}{_?abi}-{arch}", OperatingSystem.LINUX, HardwareArchitecture.ARM64, ABI.MUSL), is("v17.0.1-linux_musl-aarch64"));
    }

}
