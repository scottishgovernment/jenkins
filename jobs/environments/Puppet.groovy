package environments

import static build.Utils.trim
import static build.Utils.awsRepo

def build(site, List<String> envs) {
    dsl.job("puppet-${site.id}") {
        displayName("Puppet Apply - ${site.domain}")
        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            choiceParam('dbrestore', ['false', 'true'], 'restore databases')
        }
        scm {
            awsRepo(delegate)
        }
        steps {
            shell((trim("""\
            #!/bin/sh -e
           if [ "\$dbrestore" = "true" ]; then
             tools/management/s3_restore ${site.domain} \${env}
           fi
           cd tools/management/aws_fabric
           fab -P -z 8 --set dbrestore=\${dbrestore} \${env} apply
         """)))
        }
    }
}
