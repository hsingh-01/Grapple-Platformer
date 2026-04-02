#!/bin/bash

echo "Main-Class: Main.GrapplePlatformer" > manifest.txt

jar cfm GrapplePlatformer.jar manifest.txt -C build .

echo "JAR created: GrapplePlatformer.jar"
