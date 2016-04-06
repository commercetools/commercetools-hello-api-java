#!/usr/bin/env bash

cat > "src/main/resources/commercetools.properties" << EOF
projectKey = $PROJECT_KEY
clientId = $CLIENT_ID
clientSecret = $CLIENT_SECRET
EOF