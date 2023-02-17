#!/bin/sh

# is java_home in our environment?
if ! command -v java >/dev/null 2>&1 ; then
  if [ -f /usr/local/jdk-11/bin/java ]; then
    JAVA_HOME=/usr/local/jdk-11
  elif [ -f /usr/local/jdk-1.8.0/bin/java ]; then
    JAVA_HOME=/usr/local/jdk-1.8.0
  fi
  if [ ! -z "JAVA_HOME" ]; then
    export JAVA_HOME
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

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
