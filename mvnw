#!/bin/sh

#
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#

# -----------------------------------------------------------------------------
# Maven Wrapper script
#
# Environment Variable Preamble
# -----------------------------------------------------------------------------
#
# MVNW_REPOURL - (optional) repository URL to download maven from.
#                This variable is especially useful when using a repository manager.
#
# MVNW_USERNAME - (optional) username to use when downloading maven.
#
# MVNW_PASSWORD - (optional) password to use when downloading maven.
#
# MVNW_VERBOSE - (optional) if set to 'true' outputs more information.
#
# MAVEN_OPTS - (optional) Java options to pass to maven.
#
# JAVA_HOME - (optional) location of the Java home directory.
#
# -----------------------------------------------------------------------------

set -e

MAVEN_WRAPPER_JAR_DIR="${MAVEN_USER_HOME:-${HOME}}/.m2/wrapper/dists"
MAVEN_WRAPPER_PROPERTIES_FILE=".mvn/wrapper/maven-wrapper.properties"

# Define a default MAVEN_OPTS, if it is not already set
MAVEN_OPTS_DEFAULT="-Xms256m -Xmx512m"
if [ -z "$MAVEN_OPTS" ]; then
  MAVEN_OPTS="$MAVEN_OPTS_DEFAULT"
fi
export MAVEN_OPTS

if [ -z "$JAVA_HOME" ] ; then
  _java_executable=$(command -v java)
  if [ -n "$_java_executable" ] && [ -x "$_java_executable" ] ; then
    _java_home=$(dirname "$(dirname "$(readlink -f "$_java_executable")")")
    if [ -n "$_java_home" ] ; then
      JAVA_HOME=$_java_home
    fi
  fi
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Error: JAVA_HOME is not defined." >&2
  exit 1
fi
export JAVA_HOME

download_url() {
  if [ -f "$MAVEN_WRAPPER_PROPERTIES_FILE" ]; then
    grep -q "wrapperUrl" "$MAVEN_WRAPPER_PROPERTIES_FILE"
    if [ $? -eq 0 ]; then
      _wrapper_url=$(grep "wrapperUrl" "$MAVEN_WRAPPER_PROPERTIES_FILE" | cut -d'=' -f2)
      if [ -n "$_wrapper_url" ]; then
        if [ -n "$MVNW_REPOURL" ]; then
          _wrapper_url=$(echo "$_wrapper_url" | sed "s#https://repo.maven.apache.org/maven2#${MVNW_REPOURL}#")
        fi
        echo "$_wrapper_url"
      fi
    fi
  fi
}

download_maven() {
  _wrapper_url=$1
  _maven_wrapper_jar_path=$2
  
  if [ -n "$_wrapper_url" ]; then
    mkdir -p "$(dirname "$_maven_wrapper_jar_path")"
    if [ -n "$MVNW_VERBOSE" ]; then
      echo "Downloading $_wrapper_url to $_maven_wrapper_jar_path"
    fi
    _user_agent_args="-A 'maven-wrapper/1.0'"
    if [ -n "$MVNW_USERNAME" ] && [ -n "$MVNW_PASSWORD" ]; then
      _user_agent_args="$_user_agent_args -u $MVNW_USERNAME:$MVNW_PASSWORD"
    fi
    if command -v curl >/dev/null 2>&1; then
      curl --fail -L $_user_agent_args -o "$_maven_wrapper_jar_path" "$_wrapper_url"
    elif command -v wget >/dev/null 2>&1; then
      wget $_user_agent_args -O "$_maven_wrapper_jar_path" "$_wrapper_url"
    else
      echo "Error: Neither curl nor wget is available." >&2
      exit 1
    fi
  fi
}

_wrapper_url=$(download_url)
_maven_wrapper_jar_name=$(basename "$_wrapper_url")
_maven_wrapper_jar_path="${MAVEN_WRAPPER_JAR_DIR}/${_maven_wrapper_jar_name}"

if [ ! -f "$_maven_wrapper_jar_path" ]; then
  download_maven "$_wrapper_url" "$_maven_wrapper_jar_path"
fi

exec "$JAVA_HOME/bin/java" ${MAVEN_OPTS} -jar "$_maven_wrapper_jar_path" "$@" 