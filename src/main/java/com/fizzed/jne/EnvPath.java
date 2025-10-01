package com.fizzed.jne;

/*-
 * #%L
 * jne
 * %%
 * Copyright (C) 2016 - 2025 Fizzed, Inc
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

import java.nio.file.Path;

public class EnvPath {

    final private boolean prepend;
    final private Path value;

    public EnvPath(Path value) {
        this(value, false);
    }

    public EnvPath(Path value, boolean prepend) {
        this.prepend = prepend;
        this.value = value;
    }

    public boolean getPrepend() {
        return prepend;
    }

    public Path getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

}
