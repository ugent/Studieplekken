import requests

r = requests.post("https://studieplekken-dev.ugent.be/auth/local/tokenLink", verify=False)

print(r.json())