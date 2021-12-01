import requests

r = requests.post("https://localhost:8080/auth/local/tokenLink", verify=False)

print(r.json())