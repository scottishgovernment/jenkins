# Script to run puppet apply on the desired environment
set -e
case ${env} in
  dev)
    cd tools/management/dev
echo    fab -P -p devops --set dbrestore=${dbrestore},redisrestore=${redisrestore} ${env} puppet
    ;;
  dgv)
    cd tools/management/dev
echo    fab -P -p devops, --set dbrestore=${dbrestore},redisrestore=${redisrestore} ${env} puppet
    ;;
  *)
    cd tools/management/aws_fabric
echo    fab -P --set dbrestore=${dbrestore},redisrestore=${redisrestore} ${env} apply
    ;;
esac
