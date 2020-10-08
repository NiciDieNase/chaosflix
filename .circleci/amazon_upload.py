#!/usr/bin/env python3

import sys
import requests

client_id = sys.argv[1]
client_secret = sys.argv[2]
app_id = sys.argv[3]

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
auth_response = requests.post(amazon_auth_url, data=data)

# Read token from auth response
auth_response_json = auth_response.json()
auth_token = auth_response_json["access_token"]

auth_token_header_value = "Bearer %s" % auth_token

auth_token_header = {"Authorization": auth_token_header_value}


# Get Edits
get_edits_path = '/v1/applications/%s/edits' % app_id
get_edits_url = BASE_URL + get_edits_path
get_edits_response = requests.get(get_edits_url, headers=auth_token_header)
current_edit = get_edits_response.json()

if len(current_edit) < 1:
	# create edit
	print("Creating new edit")
	create_edit_path = '/v1/applications/%s/edits' % app_id
	create_edit_url = BASE_URL + create_edit_path
	create_edit_response = requests.post(create_edit_url, headers=auth_token_header)
	current_edit = create_edit_response.json()
else:
	print("Using existing edit")
edit_id = current_edit['id']

print(edit_id)