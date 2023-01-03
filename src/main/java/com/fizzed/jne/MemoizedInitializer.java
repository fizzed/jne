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

import java.util.concurrent.atomic.AtomicReference;

public class MemoizedInitializer<T> {

    private final AtomicReference<T> ref = new AtomicReference(null);

    public T once(Initializer<T> initializer) {
        // double lock prevention of only detecting this one time
        T value = ref.get();
        if (value == null) {
            synchronized (this) {
                // need to check it again in case two threads were waiting to build it
                value = ref.get();
                if (value == null) {
                    value = initializer.init();
                    ref.set(value);
                }
            }
        }
        return value;
    }

    public interface Initializer<T> {
        T init();
    }

}
