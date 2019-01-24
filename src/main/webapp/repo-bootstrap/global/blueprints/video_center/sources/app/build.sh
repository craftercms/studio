#!/usr/bin/env bash

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ">>> Starting app build"
echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ""

# Build the app
react-scripts build
#react-scripts-ts build

# Copy bundle on to static assets directory
cp build/static/js/main.*.js ../../static-assets/js/main.js
cp build/static/js/main.*.js.map ../../static-assets/js/main.js.map

cp build/static/css/main.*.css ../../static-assets/css/main.css
cp build/static/css/main.*.css.map ../../static-assets/css/main.css.map

# Insert the copyright banner
#cat license.txt > main.js.tmp
#cat ../../static-assets/js/app/main.js >> main.js.tmp
#mv main.js.tmp ../../static-assets/js/app/main.js

# Delete the `build` output folder
rm -rf build

echo ""
echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
echo "<<< Build completed successfully :)"
echo "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"