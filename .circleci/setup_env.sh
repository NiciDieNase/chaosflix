#!/bin/bash
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
#mkdir ~/.gradle
#GRADLEFILE=~/.gradle/gradle.properties
#echo "chaosflixKeystore=$KEYSTORE"  >> $GRADLEFILE
#echo "chaosflixStorePassword=$KEYSTORE_PASSWORD"  >> $GRADLEFILE
#echo "chaosflixKeyName=$KEY_NAME"  >> $GRADLEFILE
#echo "chaosflixKeyPassword=$KEYSTORE_PASSWORD"  >> $GRADLEFILE
#echo "appcenterId=\"$APPCENTER_ID\""  >> $GRADLEFILE
#echo "appcenterDevId=\"$APPCENTER_DEV_ID\""  >> $GRADLEFILE
#echo 'export KEYSTORE=${HOME}/code/keystore.jks'

echo "export SIGN_CONFIG=\"-PchaosflixKeystore=$KEYSTORE \
		-PchaosflixStorePassword=$KEYSTORE_PASSWORD\
		-PchaosflixKeyName=$KEY_NAME\
		-PchaosflixKeyPassword=$KEYSTORE_PASSWORD\
		-PappcenterId=$APPCENTER_ID\
		-PappcenterDevId=$APPCENTER_DEV_ID\""
