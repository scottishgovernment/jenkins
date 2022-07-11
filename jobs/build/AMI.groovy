package build

import static build.Utils.repo
import static build.Utils.trim

def build(site) {
    dsl.job("${site.id}-ami") {
        displayName("Build ${site.id} AMI")
        steps {
            def script = StringBuilder.newInstance()
            script << trim("""\
                set -ex
                repo=packer
                site=${site.id}
                version=${site.id}-\${override:-\$BUILD_ID}
            """)
            script << trim('''\
                git clean -fdx
                git update-ref --no-deref HEAD HEAD
                git tag -a -m "Build ${version}" ${version}
                git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"

                export PACKER_NO_COLOR=true
                packer validate \\
                    -var ami_name=${site}-${override:-$BUILD_ID} \\
                    -var site=${site} \\
                    templates/aws.json

                status=$(mktemp)
                trap 'rm -f "$status"' 0
                (packer build \
                    -machine-readable \\
                    -var ami_name=${site}-${override:-$BUILD_ID} \\
                    -var site=${site} \\
                    templates/aws.json || \\
                    printf "$?" > "$status") | \\
                tee build.log
                if [ -f "$status" ]; then
                    exit "$(cat "$status")"
                fi

                ami_id=$(awk -F, '$5=="id" {sub("[a-z0-9-]*:", "", $6); print $6; exit;}' build.log)

                echo '{}' | jq ".artifactId=\\"${site}\\"
                  | .name=\\"${site}-${BUILD_ID}\\"
                  | .version=${BUILD_ID}
                  | .ami=\\"${ami_id}\\"" > ami.json

                mvn deploy:deploy-file \\
                  -DgroupId=scot.mygov.ami \\
                  -DartifactId=${site} \\
                  -Dversion=${BUILD_ID} \\
                  -DrepositoryId=release \\
                  -Dpackaging=json \\
                  -DgeneratePom \\
                  -Durl=http://nexus/repository/releases/ \\
                  -Dfile=ami.json
            ''')
            shell(script.toString())
        }
        properties {
             promotions {
                  promotion {
                       name("Default")
                       icon("star-blue")
                       conditions {
                            selfPromotion(false)
                       }
                  }
             }
        }
        publishers {
            buildDescription('', "${site.id}-\${BUILD_ID}")
        }
        scm {
            git {
                remote {
                    name('origin')
                    url(repo('packer'))
                }
                branch('refs/heads/master')
            }
        }
    }
}
