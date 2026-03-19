#!/bin/bash
./gradlew clean
./gradlew --stop
rm -rf ./node_modules/
rm -rf ./.gradle/
rm ./package-lock.json
rm -rf ./src/main/resources/static/dist/
