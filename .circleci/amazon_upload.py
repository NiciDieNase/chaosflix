#!/usr/bin/env python3

import sys
import urllib.request
import urllib.parse
import json

client_id = sys.argv[1]
client_secret = sys.argv[2]
app_id = sys.argv[3]
apk_location = sys.argv[4]

BASE_URL = 'https://developer.amazon.com/api/appstore'

scope = "appstore::apps:readwrite"
grant_type = "client_credentials"
data = {
    "grant_type": grant_type,
    "client_id": client_id,
    "client_secret": client_secret,
    "scope": scope
}
amazon_auth_url = "https://api.amazon.com/auth/o2/token"
request_data = urllib.parse.urlencode(data)
request_data = request_data.encode('ascii')
auth_request = urllib.request.Request(url = amazon_auth_url, data=request_data)
auth_response = urllib.request.urlopen(auth_request)

# Read token from auth response
auth_response_json = json.load(auth_response)
auth_token = auth_response_json["access_token"]

auth_token_header_value = "Bearer %s" % auth_token

auth_token_header = {"Authorization": auth_token_header_value}


# Get Edits
get_edits_path = '/v1/applications/%s/edits' % app_id
get_edits_url = BASE_URL + get_edits_path
get_edits_request = urllib.request.Request(url=get_edits_url, headers=auth_token_header)
get_edits_response = urllib.request.urlopen(get_edits_request)
current_edit = json.load(get_edits_response)

if len(current_edit) < 1:
	# create edit
	print("Creating new edit")
	create_edit_path = '/v1/applications/%s/edits' % app_id
	create_edit_url = BASE_URL + create_edit_path
	create_edit_request = urllib.request.Request(create_edit_url, headers=auth_token_header, method="POST")
	create_edit_response = urllib.request.urlopen(create_edit_request)
	current_edit = json.load(create_edit_response)
else:
	print("Using existing edit")
edit_id = current_edit['id']
print(edit_id)

# File-Upload
print("Uploading APK (%s)" % apk_location)
add_apk_path = '/v1/applications/%s/edits/%s/apks/upload' % (app_id, edit_id)
add_apk_url = BASE_URL + add_apk_path
local_apk = open(apk_location, 'rb').read()
all_headers = {
    'Content-Type': 'application/vnd.android.package-archive'
}
all_headers.update(auth_token_header)
add_apk_request = urllib.request.Request(url=add_apk_url, headers=all_headers, data=local_apk)
add_apk_response = urllib.request.urlopen(add_apk_request)

print("Response: %s" % add_apk_response.status)