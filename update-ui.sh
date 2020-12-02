#!/bin/bash

if [ ! -d "target/ui" ]; then
	echo UI source not found, cloning
	git clone --depth 1 -b $1 https://github.com/craftercms/studio-ui.git $2
else
	echo UI source found, updating
	pushd .
	cd $2
	git pull
	popd
fi
