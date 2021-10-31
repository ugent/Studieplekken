# Load testing usingn artillery

## global install of artillery 
(best to check https://artillery.io/docs/guides/getting-started/installing-artillery.html)
'''
npm install -g artillery@latest
'''


## modules needed 
-g only if you have artillery installed globally
'''
sudo npm install -g artillery-plugin-http-ssl-auth
sudo npm install -g artillery-plugin-metrics-by-endpoint
'''
## running artillery
The reports will be outputted to report.json, name can be changed.
'''
artillery run --output report.json load_test.yml
'''
