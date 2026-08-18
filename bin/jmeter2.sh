#! /bin/sh
#
# JMeter2 launcher (Unix)
# Based on Apache JMeter startup scripts; product entry is jmeter2.sh only.
#

PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`

[ -z "$JMETER_HOME" ] && JMETER_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

if [ -r "${JMETER_HOME}/bin/setenv.sh" ]; then
  . "${JMETER_HOME}/bin/setenv.sh"
fi

if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
  if [ "`uname`" = "Darwin" ]; then
    if [ -x '/usr/libexec/java_home' ] ; then
      export JAVA_HOME=`/usr/libexec/java_home`
    elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
      export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
    fi
  else
    JAVA_PATH=`which java 2>/dev/null`
    if [ "x$JAVA_PATH" != "x" ]; then
      JAVA_PATH=`dirname "$JAVA_PATH" 2>/dev/null`
      JRE_HOME=`dirname "$JAVA_PATH" 2>/dev/null`
    fi
    if [ "x$JRE_HOME" = "x" ]; then
      if [ -x /usr/bin/java ]; then
        JRE_HOME=/usr
      fi
    fi
  fi
  if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
    echo "Neither JAVA_HOME nor JRE_HOME is defined"
    exit 1
  fi
fi
if [ -z "$JAVA_HOME" -a "$1" = "debug" ]; then
  echo "JAVA_HOME should point to a JDK in order to run in debug mode."
  exit 1
fi
if [ -z "$JRE_HOME" ]; then
  JRE_HOME="$JAVA_HOME"
fi
if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME="$JRE_HOME"
fi

JAVA9_OPTS=
MINIMAL_VERSION=8
CURRENT_VERSION=`"${JAVA_HOME}/bin/java" -version 2>&1 | awk -F'"' '/version/ {gsub("^1[.]", "", $2); gsub("[^0-9].*$", "", $2); print $2}'`
if [ "$CURRENT_VERSION" -gt "$MINIMAL_VERSION" ]; then
    JAVA9_OPTS="--add-opens java.desktop/sun.awt=ALL-UNNAMED --add-opens java.desktop/sun.swing=ALL-UNNAMED --add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED --add-opens java.desktop/java.awt=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED --add-opens=java.desktop/sun.awt.shell=ALL-UNNAMED"
fi

: "${JMETER_OPTS:=""}"
case `uname` in
   Darwin*)
   JMETER_OPTS="${JMETER_OPTS} -Xdock:name=JMeter2 -Dapple.laf.useScreenMenuBar=true -Dapple.eawt.quitStrategy=CLOSE_ALL_WINDOWS"
   ;;
esac

: "${HEAP:="-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m"}"
# Leave unset to follow OS locale; pin with language= in user.properties or export JMETER_LANGUAGE=...
: "${JMETER_LANGUAGE:=""}"
: "${GC_ALGO:="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:G1ReservePercent=20"}"

DUMP="-XX:+HeapDumpOnOutOfMemoryError"
SYSTEM_PROPS="-Djava.security.egd=file:/dev/urandom"
SERVER="-server"

if [ -z "${JMETER_COMPLETE_ARGS}" ]; then
    ARGS="$JAVA9_OPTS $SERVER $DUMP $HEAP $VERBOSE_GC $GC_ALGO $SYSTEM_PROPS $JMETER_LANGUAGE $RUN_IN_DOCKER"
else
    ARGS=""
fi

exec "$JAVA_HOME/bin/java" $ARGS $JVM_ARGS $JMETER_OPTS -jar "$PRGDIR/ApacheJMeter.jar" "$@"
