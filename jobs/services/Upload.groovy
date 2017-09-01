package services

import static build.Utils.trim

def build() {
    dsl.job("upload-cfntemplates") {
        displayName("Upload CFN Templates to s3")

        scm {
            git(repo('devnet'), 'master')
        }
        triggers {
            scm('# Poll SCM enabled to allow trigger from git hook.')
        }
        steps {
            def script = StringBuilder.newInstance()
            script << trim("""\
                set -ex
                repo=devnet
                version="1.0.${BUILD_ID}"

                git clean -fdx -e .tmp
                git tag -a -m "Build ${version}" ${version}
                git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"

                aws s3 sync $WORKSPACE/ s3://mgs-infrastructure/CloudFormationTemplates/
            """)
            shell(script.toString())
          }
        }
      }
