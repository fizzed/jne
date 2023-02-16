#!/bin/sh

# is maven available in our environment?
if ! command -v mvn >/dev/null 2>&1 ; then
  if [ -f /opt/maven/current/bin/mvn ]; then
    M2_HOME=/opt/maven/current
  elif [ -f /usr/local/maven/bin/mvn ]; then
    M2_HOME=/usr/local/maven
  fi
  if [ ! -z "M2_HOME" ]; then
    export M2_HOME
    export PATH="$M2_HOME/bin:$PATH"
  fi
fi
