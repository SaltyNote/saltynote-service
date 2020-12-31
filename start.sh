#!/usr/bin/env bash

# Check if docker is running
if ! docker info >/dev/null 2>&1; then
  echo "==================================================================="
  echo "Docker does not seem to be running, please start it first and retry"
  echo "==================================================================="
  exit 1
fi

# run docker-compose to start database
docker-compose up -d

if [[ $? -ne 0 ]]; then
  echo "=============================================================================="
  echo "Please install docker-compose, which is needed to start mysql instance locally"
  echo "==============================================================================="
  exit 1
fi

# 3. Maven build
echo "mvn clean package ..."
./mvnw clean package
if [[ $? -ne 0 ]]; then
  echo "Maven build failed!"
  exit 1
fi

# 4. Start service
# export MAVEN_OPTS=-Xmx1024m -XX:MaxPermSize=128M
echo "mvn spring-boot:run ..."
./mvnw spring-boot:run
