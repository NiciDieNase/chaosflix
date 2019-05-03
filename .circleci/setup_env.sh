#!/bin/sh
if [ "$CIRCLE_BRANCH" = "master" ] ; then
  STAGE=Prod
else
  STAGE=Dev
fi
echo "export STAGE=$STAGE"
echo "export BUILD_TYPE_LOWER=\"$(tr '[:upper:]' '[:lower:]' <<< ${BUILD_TYPE:0:1}${BUILD_TYPE:1})\""
echo "export STAGE_LOWER=\"$(tr '[:upper:]' '[:lower:]' <<< ${STAGE:0:1}${STAGE:1})\""
echo "export LIBS_LOWER=\"$(tr '[:upper:]' '[:lower:]' <<< ${LIBS:0:1}${LIBS:1})\""

echo "$ENCODED_KEYSTORE" | base64 --decode >> ${HOME}/code/keystore.jks
KEYSTORE=${HOME}/code/keystore.jks
echo >> gradle.properties
echo "chaosflixKeystore=$KEYSTORE"  >> gradle.properties
echo "chaosflixStorePassword=$KEYSTORE_PASSWORD"  >> gradle.properties
echo "chaosflixKeyName=$KEY_NAME"  >> gradle.properties
echo "chaosflixKeyPassword=$KEYSTORE_PASSWORD"  >> gradle.properties
echo "appcenterId=$APPCENTER_ID"  >> gradle.properties
echo "appcenterDevId=$APPCENTER_DEV_ID"  >> gradle.properties

echo 'export KEYSTORE=${HOME}/code/keystore.jks'
echo "export SIGN_CONFIG=\"-PchaosflixKeystore=$KEYSTORE -PchaosflixStorePassword=$KEYSTORE_PASSWORD -PchaosflixKeyName=$KEY_NAME -PchaosflixKeyPassword=$KEYSTORE_PASSWORD -PAPPCENTER_ID=$APPCENTER_ID\""
