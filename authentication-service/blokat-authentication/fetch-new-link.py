import requests

r = requests.post("http://studieplekken-dev.ugent.be/auth/local/tokenLink", verify=False)

print(r.json())