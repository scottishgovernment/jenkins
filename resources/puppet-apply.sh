# Script to run puppet apply on the desired environment
set -e
cd tools/management/dev
case "${env}" in
  dev|dgv)
    fab -P -p devops --set dbrestore=${dbrestore},redisrestore=${redisrestore} ${env} puppet;;
  *)
    fab -P -z 8 --set dbrestore=${dbrestore},redisrestore=${redisrestore} ${env} apply;;
esac
