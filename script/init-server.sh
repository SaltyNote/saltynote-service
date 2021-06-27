#!/usr/bin/env bash

echo 'update server...'
sudo apt update && sudo apt upgrade -y

echo 'install required tools...'
sudo apt install git vim curl wget zsh htop openjdk-11-jre mariadb-server nginx
#sudo apt install git vim curl wget zsh htop openjdk-11-jre tomcat9 nginx


sudo snap install core; sudo snap refresh core
sudo apt remove certbot
sudo snap install --classic certbot
sudo ln -s /snap/bin/certbot /usr/bin/certbot


echo 'create a new user, and create its home dir...'
sudo useradd -m saltynote

echo 'create Service Folder & Service Setup...'

mkdir -p /home/saltynote/service
chown saltynote:saltynote /home/saltynote/service



echo 'create systemd note.service...'

cat <<EOT >> /etc/systemd/system/note.service
[Unit]
Description=SaltyNote Service
After=syslog.target

[Service]
User=saltynote
ExecStart=/home/saltynote/service/service.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOT



#sudo mysql_secure_installation
#CREATE DATABASE saltynote;
#CREATE USER 'USER_NAME'@'localhost' IDENTIFIED BY 'A_STRONG_PASSWORD';
#GRANT ALL PRIVILEGES ON saltynote.* TO 'USER_NAME'@'localhost' WITH GRANT OPTION;
#systemctl enable note.service