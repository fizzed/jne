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

import java.util.concurrent.atomic.AtomicBoolean;

public class MemoizedRunner {

    private final AtomicBoolean ran = new AtomicBoolean(false);

    public void once(Runnable runnable) {
        // double lock prevention of only detecting this one time
        if (!ran.get()) {
            synchronized (this) {
                // need to check it again in case two threads were waiting to build it
                if (!ran.get()) {
                    runnable.run();
                }
                ran.set(true);
            }
        }
    }

}
