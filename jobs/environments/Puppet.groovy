package environments

import javaposse.jobdsl.dsl.DslFactory

import static build.Utils.trim
import static build.Utils.awsRepo

class Puppet {

    DslFactory dsl

    PrintStream out

    Puppet(dsl, out) {
        this.dsl = dsl
        this.out = out
    }

    def build(site, List<String> envs) {
        return dsl.job("puppet-${site.id}") {
            displayName("Puppet Apply - ${site.domain}")
            parameters {
                choiceParam('env', envs, "${site.domain} environment")
                choiceParam('dbrestore', ['false', 'true'], 'restore databases')
                if (site.domain == "gov.scot" ) {
                  choiceParam('redisrestore', ['false', 'true'], 'restore redis and images')
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

}
