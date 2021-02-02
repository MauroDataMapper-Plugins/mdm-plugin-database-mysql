#!/bin/bash

docker run --rm -d \
 -e MYSQL_ROOT_PASSWORD=my-secret-pw \
 -p 3306:3306 \
 --name mysql-mdm \
 mysql-mdm:latest