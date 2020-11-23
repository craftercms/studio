#!/bin/sh

if [! -d "target/ui" ]; then
	clone --depth 1 -b $1 https://github.com/craftercms/studio-ui.git $2
fi
