#!/usr/bin/env sh
SETTING_FILE=settings.gradle
SRC=library/build/outputs/aar/library-debug.aar
TARGET_DIR=demo/libs
TARGET_FILE=maybe.aar

echo "include ':library'" > ${SETTING_FILE}
./gradlew assembleDebug
mkdir -p ${TARGET_DIR}
ln -f ${SRC} ${TARGET_DIR}/${TARGET_FILE}
echo "include ':library', ':demo'" > ${SETTING_FILE}
./gradlew assembleDebug
