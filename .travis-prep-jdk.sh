#!/bin/bash
JDK_FILE=OpenJDK-jdk_x64_linux_hotspot_2021-05-06-23-30.tar.gz
JDK_URL=https://github.com/AdoptOpenJDK/openjdk17-binaries/releases/download/jdk-2021-05-07-13-31/$JDK_FILE
JDK_FOLDER=jdk-17+20

if [ -f "$HOME/downloads/$JDK_FILE" ]
then
	echo JDK already downloaded
else
	echo Downloading JDK from $JDK_URL
	pushd .
	mkdir -p "$HOME/downloads"
	cd "$HOME/downloads"
	wget -q "$JDK_URL"
	popd
fi

if [ -f "$HOME/downloads/$JDK_FOLDER/bin/java" ]
then
	echo JDK is present and unzipped, must check symlink
else
	echo Unzipping JDK
	pushd .
	cd "$HOME/downloads"
	tar xzf "$JDK_FILE"
	popd
fi

if [ -f "$HOME/jdk" ]
then
	echo JDK symlink is present
else
	echo Creating JDK symlink
	pushd .
	cd "$HOME"
	ln -sf "$HOME/downloads/$JDK_FOLDER" jdk
	popd
fi
