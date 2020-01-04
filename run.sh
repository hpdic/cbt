#!/bin/env bash

cd /Users/aren/DeepSingularity/tencent/project/CrossChain/

javac -classpath "./bin" ./src/com/nvada/blocklite/*.java -d ./bin

javac -classpath "./bin" ./src/com/nvada/blocklite/net/Master.java -d ./bin

## compile
javac -classpath "./bin" ./src/com/nvada/blocklite/net/*.java -d ./bin

## run Simulator
java -classpath "./bin" com.nvada.blocklite.Simulator

## run Master with auto recover
java -classpath "./bin" com.nvada.blocklite.net.Master Node.A true

## run Master without auto recover
java -classpath "./bin" com.nvada.blocklite.net.Master Node.A false

## run hub with port 5555
java -classpath "./bin" com.nvada.blocklite.net.Hub Node.Hub 5555

## run work with port 4444
java -classpath "./bin" com.nvada.blocklite.net.Worker Node.B 4444

## run work with port 4445
java -classpath "./bin" com.nvada.blocklite.net.Worker Node.C 4445


## run master by frame
java -classpath "./bin" com.nvada.blocklite.frame.MasterFrame


## check all port of tcp
## netstat -ntlp   

## check all usage of 80
## netstat -ntulp |grep 80

## check all usage of 4444
## netstat -an | grep 4444
