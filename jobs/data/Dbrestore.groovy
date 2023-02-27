package data

import static build.Utils.awsRepo

def build(site, List<String> envs) {
    dsl.job("dbrestore-${site.id}") {

        displayName("Restore data for ${site.domain}")

        logRotator {
            daysToKeep(90)
        }

        parameters {
            choiceParam('env', envs, 'Environment to which production data is copied')
        }

        scm {
            awsRepo(delegate)
        }

        steps {
          // Restore S3 bucket
          shell("tools/s3_restore ${site.domain} \${env}")

          // Restore RDS, MongoDB and certificates
          def script = dsl.readFileFromWorkspace('resources/dbrestore')
          shell(script)
        }

        publishers {
            buildDescription('', '${env}')
        }

    }
}
