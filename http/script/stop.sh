#!/bin/bash

rm -rf start.out

dirpath="$(cd "$(dirname "$0")" && pwd)"
cd $dirpath

JAVA_HOME=$JAVA_HOME
APP_MAIN=cn.webank.wedpr.http.PpcsPirApplication
CURRENT_DIR=`pwd`
CONF_DIR=${CURRENT_DIR}/conf

# 初始化全局变量，用于标识系统的PID（0表示未启动）
tradePortalPID=0

getTradeProtalPID(){
    javaps=`$JAVA_HOME/bin/jps -l | grep $APP_MAIN`
    if [ -n "$javaps" ]; then
        tradePortalPID=`echo $javaps | awk '{print $1}'`
    else
        tradePortalPID=0
    fi
}

stop(){
    getTradeProtalPID
    echo "==============================================================================================="
    if [ $tradePortalPID -ne 0 ]; then
        echo -n "Stopping $APP_MAIN(PID=$tradePortalPID)..."
        kill $tradePortalPID > /dev/null
        if [ $? -eq 0 ]; then
            echo "[Success]"
            echo "==============================================================================================="
        else
            echo "[Failed]"
            echo "==============================================================================================="
        fi
        getTradeProtalPID
    else
        echo "$APP_MAIN is not running"
        echo "==============================================================================================="
    fi
}

stop
