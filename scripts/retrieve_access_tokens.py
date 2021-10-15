import requests
import sys
import os

TEST_USERS = int(sys.argv[1])
REQUEST_LINK = 'https://studieplekken-dev.ugent.be/auth/login/test'
TEXT_FILE = "access_tokens.txt"

if os.path.exists(TEXT_FILE):
    os.remove(TEXT_FILE)
response = requests.get(REQUEST_LINK)

with open(TEXT_FILE, 'a') as file:
    print(TEST_USERS)
    for i in range(TEST_USERS):
        
        access_token = response.json()["access_token"]
        print(access_token)
        response = requests.get(REQUEST_LINK)
        file.write(access_token + "\n")