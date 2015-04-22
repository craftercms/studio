#!/bin/bash

. /Users/rdanner/crafter-installs/2-5-0-test-1/scripts/setenv.sh

CRAFTER_START="/Users/rdanner/crafter-installs/2-5-0-test-1/crafter-deployer/start-deploy-agent.sh"
CRAFTER_STOP="/Users/rdanner/crafter-installs/2-5-0-test-1/crafter-deployer/stop-deploy-agent.sh"
CRAFTER_DIRECTORY="/Users/rdanner/crafter-installs/2-5-0-test-1/crafter-deployer/"
CRAFTER_PROGRAM="org.craftercms.cstudio.publishing.PublishingReceiverMain"
CRAFTER_PID=""
CRAFTER_STATUS=""
ERROR=0

is_service_running() {
    CRAFTER_PID=`ps ax | grep "$1" | grep -v "grep" | awk '{print $1}' 2>&1`
    if [ $CRAFTER_PID ] ; then
        RUNNING=1
    else
        RUNNING=0
    fi
    return $RUNNING
}

is_crafter_running() {
    is_service_running "$CRAFTER_PROGRAM"
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
        CRAFTER_STATUS="Crafter deployer not running"
    else
        CRAFTER_STATUS="Crafter deployer already running"
    fi
    return $RUNNING
}

start_crafter() {
    is_crafter_running
    RUNNING=$?
    if [ $RUNNING -eq 1  ]; then
        echo "$0 $ARG: Crafter deployer (pid $CRAFTER_PID) already running"
        exit
    else 
        cd $CRAFTER_DIRECTORY
        $CRAFTER_START > /dev/null 2>&1 &
    fi

    sleep 2
    is_crafter_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
        ERROR=1
    fi
    if [ $ERROR -eq 0 ]; then
	echo "$0 $ARG: Crafter deployer  started"
	sleep 2
    else
	echo "$0 $ARG: Crafter deployer could not be started"
	ERROR=3
    fi

}

stop_crafter() {
    NO_EXIT_ON_ERROR=$1
    is_crafter_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
        echo "$0 $ARG: $CRAFTER_STATUS"
        if [ "x$NO_EXIT_ON_ERROR" != "xno_exit" ]; then
            exit
        else
            return
        fi
    fi
	
    cd $CRAFTER_DIRECTORY
    $CRAFTER_STOP > /dev/null 2>&1
    sleep 6
    is_crafter_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
            echo "$0 $ARG: Crafter deployer stopped"
        else
            echo "$0 $ARG: Crafter deployer could not be stopped"
            ERROR=4
    fi
}

if [ "x$1" = "xstart" ]; then
    start_crafter
elif [ "x$1" = "xstop" ]; then
    stop_crafter
elif [ "x$1" = "xstatus" ]; then
    is_crafter_running
    echo "$CRAFTER_STATUS"
fi

exit $ERROR
