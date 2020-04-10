#!/bin/bash
# based on https://gist.github.com/shane-harper/cd3b3c8cf79d70e8ce2d2484bde28d9d
owner_name=$1
token=$2
build_path=$3
release_notes_file=$4
destination_name=$5
mapping_path=$6
versionCode=$7
versionName=$8

if [ "$CIRCLE_BRANCH" = "master" ] ; then
	app_name=chaosflix
elif [ "$CIRCLE_BRANCH" = "develop" ] ; then
	app_name=Chaosflix-Dev
else
	exit 0
fi

# Step 1: Create an upload resource and get an upload_url (good for 24 hours)
request_url="https://api.appcenter.ms/v0.1/apps/${owner_name}/${app_name}/release_uploads"
upload_json=$(curl -X POST \
	--header "Content-Type: application/json" \
	--header "Accept: application/json" \
	--header "X-API-Token: ${token}" \
	"${request_url}" 2> /dev/null)
upload_id=$(echo ${upload_json} | \
    python3 -c "import sys, json; print(json.load(sys.stdin)['upload_id'])")
upload_url=$(echo ${upload_json} | \
    python3 -c "import sys, json; print(json.load(sys.stdin)['upload_url'])")
echo ${upload_json}

# Step 2: Upload ipa
curl -F "ipa=@${build_path}" ${upload_url}

# Step 3: Upload resource's status to committed and get a release_url
release_json=$(curl -X PATCH \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header "X-API-Token: ${token}" \
-d '{ "status": "committed" }' \
"${request_url}/${upload_id}" 2> /dev/null)
release_id=$(echo ${release_json} | \
    python3 -c "import sys, json; print(json.load(sys.stdin)['release_id'])")
echo "${release_json}"

release_notes=""
while read -r line; do
	release_notes="${release_notes}* ${line}\n"
done < "${release_notes_file}"
release_notes="$(tr '"' "'" <<< $release_notes)"
release_notes=${release_notes::5000}

# Step 4: Distribute the uploaded release to a distribution group"
release_url="https://api.appcenter.ms/v0.1/apps/${owner_name}/${app_name}/releases/${release_id}"
data="{ \"destination_name\": \"${destination_name}\", \"release_notes\": \"${release_notes}\" }"
echo ${data}
response_json=$(curl -X PATCH --header 'Content-Type: application/json' --header 'Accept: application/json' --header "X-API-Token: ${token}" -d "${data}" ${release_url})
echo ${response_json}

if [ -n "$mapping_path"  ]; then
	#step 1 get upload id and url
	request_url="https://api.appcenter.ms/v0.1/apps/${owner_name}/${app_name}/symbol_uploads"
	data="{\"symbol_type\": \"AndroidProguard\",\"file_name\": \"mapping.txt\",\"build\": \"$versionCode\",\"version\": \"$versionName\"}"
	mapping_json=$(curl -X POST \
		-d "$data" \
		--header "Content-Type: application/json" \
		--header "Accept: application/json" \
		--header "X-API-Token: ${token}" \
		"${request_url}" 2> /dev/null)
	echo $mapping_json
	upload_id=$(echo ${mapping_json} | \
    	python3 -c "import sys, json; print(json.load(sys.stdin)['symbol_upload_id'])")
	upload_url=$(echo ${mapping_json} | \
    	python3 -c "import sys, json; print(json.load(sys.stdin)['upload_url'])")
    # step 2 upload mappings
    upload_response=$(curl -X PUT \
    	--header 'Accept: application/json' \
    	--header "X-API-Token: ${token}" \
    	--header "x-ms-blob-type: BlockBlob" \
    	-F "ipa=@$mapping_path" \
    	"$upload_url" 2> /dev/null)
	# step 3 confirm
	confirm_url="https://api.appcenter.ms/v0.1/apps/${owner_name}/${app_name}/symbol_uploads/${upload_id}"
	data="{\"status\": \"committed\"}"
	response_json=$(curl -X PATCH \
		--header 'Content-Type: application/json' \
		--header 'Accept: application/json' \
		--header "X-API-Token: ${token}" \
		-d "${data}" \
		${confirm_url} 2> /dev/null)
fi
