set -e
if [ -d "reports" ]; then
   echo "removing old reports";
   rm -fR reports/*;
fi

./run.sh -t web 
