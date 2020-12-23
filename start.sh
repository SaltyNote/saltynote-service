#!/usr/bin/env bash

# 1. check email configuration
email_conf=$(cat src/main/resources/application.properties | grep '${MAIL_USERNAME:}')
if [[ -n "${email_conf}" ]]; then
  echo "========================================================================================"
  echo "Error: Email configuration is needed."
  echo "You can configuration your gmail info inside 'src/main/resources/application.properties'"
  echo "For more info, please follow this link https://git.io/JLP6q".
  echo "Note: Please do not commit your gmail credentials."
  echo "========================================================================================"
  exit 1
fi

# 2. check docker installation
docker-compose up -d

if [[ $? -ne 0 ]]; then
  echo "======================================================================================================"
  echo "Please install & start docker(docker-compose) service, which is needed to start mysql instance locally"
  echo "======================================================================================================"
  exit 1
fi

# 3. Maven build
echo "mvn clean package ..."
mvn clean package
if [[ $? -ne 0 ]]; then
  echo "Maven build failed!"
  exit 1
fi

# 4. Start service
# export MAVEN_OPTS=-Xmx1024m -XX:MaxPermSize=128M
echo "mvn spring-boot:run ..."
mvn spring-boot:run
