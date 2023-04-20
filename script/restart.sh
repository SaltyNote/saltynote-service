#!/usr/bin/env bash

randStr=$(openssl rand -base64 17)
randStr=${randStr//[\/+=]/0}
export ACTUATOR_PATH=${randStr}

systemctl restart note.service