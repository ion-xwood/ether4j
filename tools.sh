#!/bin/bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 
   exit 1
fi

IMAGE="ethereum/client-go:release-1.13"
CONTAINER="geth"
PATH_DATA="/var/geth"
RPC_PORT=8545
WS_PORT=8546
PATH_IPC="/var/run/geth.ipc"
GETH_API="admin,debug,miner,shh,txpool,personal,eth,net,web3,db"

function purge(){
    docker stop $CONTAINER 
    docker rmi -f $IMAGE
}

function pull(){
    purge
    docker image pull $IMAGE
    docker container list --all
}

function destroy(){
    docker container rm -f $CONTAINER
    docker container list --all
}

function create(){
    mkdir -p $PATH_DATA
    docker container rm -f $CONTAINER
    docker container create \
        -p $RPC_PORT:$RPC_PORT \
        -p $WS_PORT:$WS_PORT \
        -v $PATH_DATA:/root \
        -v /var/run:/var/run \
        --name $CONTAINER \
        $IMAGE \
        --rpc --rpcaddr 0.0.0.0 --rpcport $RPC_PORT --rpccorsdomain "*" --rpcapi $GETH_API \
        --ws --wsaddr 0.0.0.0 --wsport $WS_PORT --wsorigins "*" --wsapi $GETH_API \
        --ipcpath=$PATH_IPC \
        --dev
    docker container list -a
}

function stop(){ 
    docker stop $CONTAINER
    docker ps
}
function start(){ 
    docker start $CONTAINER
    docker ps
}

function dev(){
    mkdir -p $PATH_DATA
    docker stop $CONTAINER
    docker container rm -f $CONTAINER
    docker run --rm -it  \
        -p $RPC_PORT:$RPC_PORT \
        -p $WS_PORT:$WS_PORT \
        -v $PATH_DATA:/root \
        -v /var/run:/var/run \
        --name $CONTAINER \
        $IMAGE \
        --rpc --rpcaddr 0.0.0.0 --rpcport $RPC_PORT --rpccorsdomain "*" --rpcapi $GETH_API \
        --ws --wsaddr 0.0.0.0 --wsport $WS_PORT --wsorigins "*" --wsapi $GETH_API \
        --ipcpath=$PATH_IPC \
        --dev \
        --verbosity 4\
        --debug
}

function console(){ 
    docker exec -it  $CONTAINER /usr/local/bin/geth attach ipc:$PATH_IPC
}

function enter(){ 
    docker exec -it  $CONTAINER /bin/sh
}

function help(){
    echo "Help:"
    echo "    help"
    echo "    start"
    echo "    stop"
    echo "    dev"
    echo "    console"
    echo "    create"
    echo "    destroy"
    echo "    pull"
    echo "    purge"
    echo "    enter"
}

if [[ -z $1 ]]; then help; else set -x; $1; fi

