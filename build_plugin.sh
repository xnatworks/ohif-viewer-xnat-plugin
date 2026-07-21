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

# PLUGINS-294: the About dialog shows extension-xnat's package.json version.
# Keep it in sync with the plugin version (single source of truth: build.gradle).
PLUGIN_VERSION=$(sed -n 's/.*vPluginVersion = "\([^"]*\)".*/\1/p' "${PLUGIN_ROOT}/build.gradle" | head -1)
VIEWER_PKG="${VIEWER_ROOT}/extensions/xnat/package.json"
if [[ -n "${PLUGIN_VERSION}" && -f "${VIEWER_PKG}" ]]; then
    VIEWER_VERSION=$(node -p "require('${VIEWER_PKG}').version")
    if [[ "${VIEWER_VERSION}" != "${PLUGIN_VERSION}" ]]; then
        echo "WARNING: extension-xnat version (${VIEWER_VERSION}) != plugin version (${PLUGIN_VERSION})"
        echo "WARNING: injecting ${PLUGIN_VERSION} for this build; please sync extensions/xnat/package.json"
        node -e "const fs=require('fs');const p=process.argv[1];const j=JSON.parse(fs.readFileSync(p,'utf8'));j.version=process.argv[2];fs.writeFileSync(p,JSON.stringify(j,null,2)+'\n');" "${VIEWER_PKG}" "${PLUGIN_VERSION}"
    fi
else
    echo "WARNING: could not determine plugin version or find ${VIEWER_PKG}; skipping version injection"
fi

yarn config set workspaces-experimental true
yarn install --check-files
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

