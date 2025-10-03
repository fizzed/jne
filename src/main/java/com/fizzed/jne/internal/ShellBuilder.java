package com.fizzed.jne.internal;

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

import com.fizzed.jne.EnvPath;
import com.fizzed.jne.EnvVar;
import com.fizzed.jne.ShellType;

public class ShellBuilder {

    private final ShellType shellType;

    public ShellBuilder(ShellType shellType) {
        this.shellType = shellType;
    }

    public String exportEnvVar(EnvVar var) {
        switch (shellType) {
            case SH:
            case BASH:
            case ZSH:
            case KSH:
                return "export " + var.getName() + "=\"" + var.getValue() + "\"";
            case CSH:
                return "setenv " + var.getName() + " \"" + var.getValue() + "\"";
            default:
                throw new IllegalArgumentException("Unsupported shell type: " + shellType);
        }
    }

    public String addEnvPath(EnvPath path) {
        switch (shellType) {
            case SH:
            case BASH:
                if (path.isPrepend()) {
                    return "case \":$PATH:\" in *:\"" + path.getValue() + "\":*) ;; *) PATH=\"" + path.getValue() + "${PATH:+:$PATH}\" ;; esac; export PATH";
                } else {
                    return "case \":$PATH:\" in *:\"" + path.getValue() + "\":*) ;; *) PATH=\"${PATH:+$PATH:}" + path.getValue() + "\" ;; esac; export PATH";
                }
            case ZSH:
                if (path.isPrepend()) {
                    return "[[ ! \"$PATH\" =~ (^|:)" + path.getValue() + "(:|$) ]] && export PATH=\"$PATH:" + path.getValue() + "\"";
                } else {
                    return "[[ ! \"$PATH\" =~ (^|:)" + path.getValue() + "(:|$) ]] && export PATH=\"" + path.getValue() + ":$PATH\"";
                }
            default:
                throw new IllegalArgumentException("Unsupported shell type: " + shellType);
        }
    }

}
