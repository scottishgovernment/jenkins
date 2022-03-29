package data

import static build.Utils.awsRepo

def build(site, List<String> envs) {
    dsl.job("copy-s3-${site.id}") {
        displayName("Restore S3 data for ${site.domain}")
        parameters {
            choiceParam('env', envs, 'Environment to which production S3 data is copied')
        }
        scm {
            awsRepo(delegate)
        }
        steps {
            shell("tools/s3_restore ${site.domain} \${env}")
        }
        publishers {
            buildDescription('', '${env}')
        }
    }
}
