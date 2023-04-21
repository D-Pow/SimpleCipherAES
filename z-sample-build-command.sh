#!/usr/bin/env -S bash

# ./gradlew clean jar
./gradlew cleanAll jar
ls -FlAh build/libs/
unzip -Z build/libs/*.jar | wc -l
java -jar build/libs/simplecipheraes.jar
unzip -uoq build/libs/simplecipheraes.jar -d build/libs/simplecipheraes/
