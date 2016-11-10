package services

import static build.Utils.trim

def build() {
    dsl.job("apply-services") {
        displayName("Apply Services Puppet")



        steps {
          shell(trim('''\

        source=/var/lib/jenkins/jobs/mygov-seed/workspace/resources/fabfile.py\n
        target=/var/lib/jenkins/jobs/apply-services/workspace/services/\n

        if [ ! -d "$target" ] ; then\n
           mkdir $target\n
           cp $source $target\n
        else\n
           echo "Fabfile is in place already"\n
        fi\n

        cd services\n
	      fab -P servers apply\n
 	      '''))
        }
     }
 }
