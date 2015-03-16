#!/bin/bash
rm -f scalamake-*.zip
mkdir scalamake
cd scalamake
cp ../target/scala*/*.jar .
cp ../skel/* .
cd ..
zip -r ../scalamake_0.1.0.zip scalamake
rm -fr scalamake
