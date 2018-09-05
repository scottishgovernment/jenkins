package data

import static build.Utils.awsRepo

def build(site, List<String> envs) {
    dsl.job("dbrestore-${site.id}") {

        displayName("Restore RDS, mongodb and s3 data for ${site.domain}")

        parameters {
            choiceParam('env', envs, 'Environment to which production data is copied')
        }
        scm {
            awsRepo(delegate)
        }
        steps {
          // Duplication of Restore.groovy
          shell("tools/management/s3_restore ${site.domain} \${env}")

          // RDS and mongodb
          def script = dsl.readFileFromWorkspace('resources/dbrestore')
          shell(script)
        }

        publishers {
            buildDescription('', '${env}')
        }
    }
}
