#!/bin/sh
VERSION_FILE="versionfile"
VERSION_NAME=$(cat ${VERSION_FILE} | tr -d '\n\r')

case $1 in
	--touch|-t|touch) printf "${VERSION_NAME}-touch" ;;
	--leanback|-l|leanback) printf "${VERSION_NAME}-leanback" ;;
	--dev) printf "${VERSION_NAME}-develop" ;;
	*) printf "${VERSION_NAME}" ;;
esac