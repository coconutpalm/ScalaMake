#!/bin/bash
mkdir temp
cd temp
cp ../target/*.jar .
cp ../skel/* .
zip ../scalamake *
cd ..
rm -fr temp
