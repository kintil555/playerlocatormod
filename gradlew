#!/bin/sh
# Gradle wrapper script
APP_HOME=$(dirname "$(readlink -f "$0")" 2>/dev/null || dirname "$0")
exec java -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
