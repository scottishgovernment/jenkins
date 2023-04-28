package services

import static build.Utils.repo
import static build.Utils.trim

def build() {
    dsl.job("devnet") {
        displayName("Devnet VPC")

        scm {
            git(repo('devnet'))
        }
        triggers {
            scm('# Poll SCM enabled to allow trigger from git hook.')
        }
        steps {
            def script = StringBuilder.newInstance()
            shell(trim('''\
                #!/bin/sh
                set -ex
                repo=devnet
                version="1.0.${BUILD_ID}"

                git tag -a -m "Build ${version}" ${version}
                git push --tags origin "${version}"

                aws s3 sync $WORKSPACE/ s3://mgs-infrastructure/CloudFormationTemplates/
            '''))
            shell(script.toString())
          }
        }
      }
