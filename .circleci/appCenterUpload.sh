#!/bin/sh
# based on https://gist.github.com/shane-harper/cd3b3c8cf79d70e8ce2d2484bde28d9d
owner_name=$1
token=$2
build_path=$3
release_notes=$4
destination_name=$5

if [ "$CIRCLE_BRANCH" = "master" ] ; then
	app_name=chaosflix
elif [ "$CIRCLE_BRANCH" = "develop" ] ; then
	app_name=Chaosflix-Dev
else
	exit 0
fi

# Step 1: Create an upload resource and get an upload_url (good for 24 hours)
request_url="https://api.appcenter.ms/v0.1/apps/${owner_name}/${app_name}/release_uploads"
upload_json=$(curl -X POST --header "Content-Type: application/json" --header "Accept: application/json" --header "X-API-Token: ${token}" "${request_url}") 
upload_id=$(echo ${upload_json} | \
    python3 -c "import sys, json; print(json.load(sys.stdin)['upload_id'])")
upload_url=$(echo ${upload_json} | \
    python3 -c "import sys, json; print(json.load(sys.stdin)['upload_url'])")

# Step 2: Upload ipa
curl -F "ipa=@${build_path}" ${upload_url}

# Step 3: Upload resource's status to committed and get a release_url
release_json=$(curl -X PATCH --header 'Content-Type: application/json' --header 'Accept: application/json' --header "X-API-Token: ${token}" -d '{ "status": "committed" }' "${request_url}/${upload_id}")
release_id=$(echo ${release_json} | \
    python3 -c "import sys, json; print(json.load(sys.stdin)['release_id'])")

# Step 4: Distribute the uploaded release to a distribution group"
release_url="https://api.appcenter.ms/v0.1/apps/${owner_name}/${app_name}/releases/${release_id}"
data="{ \"destination_name\": \"${destination_name}\", \"release_notes\": \"${release_notes}\" }"
response_json=$(curl -X PATCH --header 'Content-Type: application/json' --header 'Accept: application/json' --header "X-API-Token: ${token}" -d "${data}" ${release_url})
echo ${response_json}