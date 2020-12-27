#!/bin/bash
# Watch and output new node-sass scss

# Do it once
npx node-sass  --importer node_modules/node-sass-package-importer/dist/cli.js --output resources/public/css/ styles/

# Then do it each time things change
npx node-sass  --importer node_modules/node-sass-package-importer/dist/cli.js --output resources/public/css/ styles/ --watch
