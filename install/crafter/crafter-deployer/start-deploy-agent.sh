#!/bin/sh
nohup java -server  -Djava.ext.dirs=. -classpath .:conf org.craftercms.cstudio.publishing.PublishingReceiverMain &
