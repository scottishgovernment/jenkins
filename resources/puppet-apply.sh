# Script to run puppet apply on the desired environment
set -e
case "${env}" in
  dev|dgv)
    cd tools/management/dev
    fab -P --set dbrestore=${dbrestore},imagesrestore=${imagesrestore},redisrestore=${redisrestore} ${env} puppet;;
  *)
    cd tools/management/aws_fabric
    fab -P -z 8 --set dbrestore=${dbrestore},imagesrestore=${imagesrestore},redisrestore=${redisrestore} ${env} apply;;
esac
