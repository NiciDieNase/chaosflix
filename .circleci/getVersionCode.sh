#!/bin/sh
COUNT=$(git rev-list --count HEAD | tr -d '\n\r')
COUNT=$(expr $COUNT - 800)
if [ $(expr $COUNT % 2) -ne 0 ]; then
	COUNT=$(expr $COUNT - 1)
fi
case $1 in
	--touch|-t|touch) printf $COUNT ;;
	--leanback|-l|leanback) printf $(expr $COUNT - 1) ;;
esac