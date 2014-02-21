#!/bin/bash -xe

echo "Building QR Code Lib"
android update lib-project --target android-17 --path .
ant clean
ant release
