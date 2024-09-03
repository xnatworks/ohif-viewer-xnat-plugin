#!/usr/bin/env bash

PLUGIN_ROOT="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
VIEWER_ROOT=${PLUGIN_ROOT}/ohifviewerxnat
VIEWER_DIST=${VIEWER_ROOT}/platform/viewer/dist
VIEWER_TARGET=${PLUGIN_ROOT}/src/main/resources/META-INF/resources/VIEWER

[[ -z "${1}" ]] && {
    BUILD_ARGS="clean fatJar"
} || {
    BUILD_ARGS="${@}"
    [[ $(echo "${BUILD_ARGS}" | grep -iF fatJar | wc -l | tr -d ' ') == 0 ]] && { BUILD_ARGS="fatJar ${BUILD_ARGS}"; }
    [[ $(echo "${BUILD_ARGS}" | grep -iF clean | wc -l | tr -d ' ') == 0 ]] && { BUILD_ARGS="clean ${BUILD_ARGS}"; }
}

echo "OHIF Viewer XNAT plugin build: ${PLUGIN_ROOT}"
echo "Using build args: ${BUILD_ARGS}"

cd "${PLUGIN_ROOT}"
echo "Cleaning: ${VIEWER_TARGET}"
rm -rf "${VIEWER_TARGET}/"*
mkdir -p "${VIEWER_TARGET}"

cd "${VIEWER_ROOT}"
echo "Building OHIF Viewer: "`pwd`
yarn config set workspaces-experimental true
yarn install
yarn run build:xnat
if [ $? -ne 0 ]; then
	exit
fi
cd "${VIEWER_DIST}"
cp -rf * "${VIEWER_TARGET}"

cd "${PLUGIN_ROOT}"
echo "Building plugin: "`pwd`
./gradlew ${BUILD_ARGS}
if [ $? -eq 0 ]; then
	echo "Build complete"
fi

