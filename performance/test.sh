#!/usr/bin/env sh

# optional first argument is a base URL for the endpoints to be tested
# remember to quote the URL, so for example:
# ./test.sh 'http://localhost'

# determine base uri from first argument or use default
URL='https://test.openlmis.org'
if [ ! -z ${1+x} ]; then
  URL=$1
fi

MEM_LIMIT=4g
if [ ! -z ${2+x} ]; then
  MEM_LIMIT=$2
fi

echo "Running performance tests against: $URL"
export BASE_URL="${URL}"
docker run --rm -e BASE_URL -v $(pwd):/bzt-configs \
  -v $(pwd)/../build/performance-artifacts:/tmp/artifacts \
  -v $(pwd)/resources:/tmp/resources \
  --memory=${MEM_LIMIT} \
  blazemeter/taurus:1.10.3 \
  -o modules.jmeter.properties.base-uri="${BASE_URL}" \
  -o reporting.2.dump-xml=/tmp/artifacts/stats.xml \
  config.yml \
  tests/*.yml
