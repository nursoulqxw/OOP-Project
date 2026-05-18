#!/bin/bash
set -e
cd "$(dirname "$0")"
mkdir -p out
find src -name "*.java" > /tmp/uni_sources.txt
javac -d out @/tmp/uni_sources.txt
java -cp out tests.Main