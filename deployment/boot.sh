#!/bin/bash
status=$(docker-compose ps -q | wc -l)
if [ "$status" -gt 0 ]; then
    echo "Docker Compose 服务正在运行，将关闭服务..."
    docker-compose down
else
    echo "Docker Compose 服务当前未运行，将启动服务..."
    docker-compose up -d --build
fi