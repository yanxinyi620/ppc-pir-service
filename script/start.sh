#!/bin/bash

dirpath="$(cd "$(dirname "$0")" && pwd)"
cd $dirpath

JAVA_HOME=$JAVA_HOME
APP_MAIN=cn.webank.wedpr.pir.PpcsPirApplication
CLASSPATH='conf/:app/*:libs/*'
CURRENT_DIR=`pwd`
LOG_DIR=${CURRENT_DIR}/logs
CONF_DIR=${CURRENT_DIR}/conf
os_platform=`uname -s`


if [ "${JAVA_HOME}" = "" ];then
    echo "JAVA_HOME has not been configured"
    exit -1
fi

mkdir -p logs

# 初始化全局变量，用于标识系统的PID（0表示未启动）
tradePortalPID=0
start_timestamp=0
JMXREMOTE_PORT=22223
APP_NAME="wedpr-pir"

getTradeProtalPID(){
    javaps=`$JAVA_HOME/bin/jps -l | grep $APP_MAIN`
    if [ -n "$javaps" ]; then
        tradePortalPID=`echo $javaps | awk '{print $1}'`
    else
        tradePortalPID=0
    fi
}

#JAVA_OPTS=" -Dfile.encoding=UTF-8 -Dcom.sun.management.jmxremote.port=${JMXREMOTE_PORT} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPTS=" -Dlog4j.configurationfile=${CONF_DIR}/log4j2.xml -Dindex.log.home=${LOG_DIR} -Dconfig=${CONF_DIR}/"
JAVA_OPTS+=" -Xmx2048m -Xms2048m -XX:NewSize=1024m -XX:MaxNewSize=1024m -XX:PermSize=512m -XX:MaxPermSize=512m"
JAVA_OPTS+=" -XX:CompileThreshold=20000 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70"
JAVA_OPTS+=" -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${LOG_DIR}/jvm.log"
JAVA_OPTS+=" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}/ -XX:ErrorFile=${LOG_DIR}/heap_error.log"

function get_start_time() {
    start_time=$(date "+%Y-%m-%d %H:%M:%S")
    if [[ "${os_platform}" = "Darwin" ]];then
        start_timestamp=$(date -j -f "%Y-%m-%d %H:%M:%S" "${start_time}" +%s)
    elif [[ "${os_platform}" = "Linux" ]];then
        start_timestamp=$(date -d "${start_time}" +%s)
    else
        echo -e "\033[31m[ERROR] Server start fail!\033[0m"
        echo "check platform...[failed]"
        echo "==============================================================================================="
        kill $tradePortalPID
        exit 1
    fi
}

function waiting_for_start() {
    echo ""
    i=0
    while [ $i -le 30 ]
    do
        for j in '\\' '|' '/' '-'
        do
            printf "%c%c%c%c%c Waiting for server started %c%c%c%c%c\r" \
            "$j" "$j" "$j" "$j" "$j" "$j" "$j" "$j" "$j" "$j"
            check_time=$(cat ${dirpath}/logs/${APP_NAME}.log | grep -a "JVM running for" | tail -n1 | awk -F "]" '{print $3}' | awk -F "[" '{print $2}' | awk -F " " '{print $1, $2}')
            if [ -n "$check_time" ]; then
                if [[ "${os_platform}" = "Darwin" ]];then
                    timestamp=$(date -j -f "%Y-%m-%d %H:%M:%S" "${check_time}" +%s)
                elif [[ "${os_platform}" = "Linux" ]];then
                    timestamp=$(date -d "${check_time}" +%s)
                else
                    echo -e "\033[31m[ERROR] Server start fail!\033[0m"
                    echo "check platform...[failed]"
                    echo "==============================================================================================="
                    kill $tradePortalPID
                    exit 1
                fi
                if [[ ${timestamp} -gt ${start_timestamp} ]]; then
                    echo ""
                    echo -e "\033[32m[INFO] Server start Successful!\033[0m"
                    echo "(PID=$tradePortalPID)...[Success]"
                    echo "==============================================================================================="
                    exit 0
                fi
            fi
            sleep 1
        done
        let i=i+5
    done
    echo ""
    echo -e "\033[31m[ERROR] Server start fail!\033[0m"
    echo "timeout...[failed]"
    echo "==============================================================================================="
    kill $tradePortalPID
    exit 1
}

start(){
    getTradeProtalPID
    echo "==============================================================================================="
    if [ $tradePortalPID -ne 0 ]; then
        echo "$APP_MAIN already started(PID=$tradePortalPID)"
        echo "==============================================================================================="
    else
        get_start_time
        nohup $JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASSPATH $APP_MAIN  >start.out 2>&1 &
        sleep 1
        getTradeProtalPID
        if [ $tradePortalPID -ne 0 ]; then
            waiting_for_start
        else
            echo -e "\033[31m[ERROR] Server start fail!\033[0m"
            echo "[Failed]"
            echo "==============================================================================================="
        fi
    fi
}

start
