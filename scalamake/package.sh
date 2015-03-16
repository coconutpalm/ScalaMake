#!/bin/bash
rm -y scalamake-*.zip
mkdir temp
cd temp
cp ../target/scala*/*.jar .
cp ../skel/* .
zip ../scalamake *
cd ..
#rm -fr temp
