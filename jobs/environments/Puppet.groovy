package environments

import static build.Utils.trim
import static build.Utils.awsRepo

def build(site, List<String> envs) {
    dsl.job("puppet-${site.id}") {
        displayName("Puppet Apply - ${site.domain}")
        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            choiceParam('dbrestore', ['false', 'true'], 'restore databases')
            if (site.domain == "gov.scot" ) {
              choiceParam('redisrestore', ['false', 'true'], 'restore redis and images')
              choiceParam('imagesrestore', ['false', 'true'], 'restore images')
            }
        }
        scm {
            awsRepo(delegate)
        }
        steps {
            shell((trim("""\
              if [ "\$dbrestore" = "true" ]; then
                tools/management/s3_restore ${site.domain} \${env}
              fi
            """)))
            shell(dsl.readFileFromWorkspace('resources/puppet-apply.sh'))
        }
    }
}
