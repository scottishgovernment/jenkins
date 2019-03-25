import static build.Utils.repo
import static build.Utils.trim

def view = []

def sites = [ 'mygov', 'gov' ]

sites.each { site ->

    view << job(site + '-ami') {
        displayName("Build ${site} AMI")
        steps {
            def script = StringBuilder.newInstance()
            script << trim("""\
                set -ex
                repo=packer
                version=${site}-\${override:-\$BUILD_ID}

                git clean -fdx
                git update-ref --no-deref HEAD HEAD
                git tag -a -m "Build \${version}" \${version}

                git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/\${repo}.git "\${version}"
            """)
            script << trim("""\
                packer validate \\
                    -var ami_name=${site}-\${override:-\$BUILD_ID} \\
                    templates/aws.json
            """)
            script << trim("""\
                export PACKER_NO_COLOR=true
                packer build \\
                    -var ami_name=${site}-\${override:-\$BUILD_ID} \\
                    templates/aws.json -machine-readable | tee build.log
                    ami_id=\$(egrep -w -m1 -o ami-[[:xdigit:]]+ build.log)
            """)
            script << trim("""\
            echo '{}' | jq ".artifactId=\\"${site}\\"
              | .name=\\"${site}-\${BUILD_ID}\\"
              | .version=\${BUILD_ID}
              | .ami=\\"\${ami_id}\\"" > ami.json

            mvn deploy:deploy-file \\
            -DgroupId=scot.mygov.ami \\
            -DartifactId=${site} \\
            -Dversion=\${BUILD_ID} \\
            -DrepositoryId=release \\
            -Dpackaging=json \\
            -DgeneratePom \\
            -Durl=http://nexus/repository/releases/ \\
            -Dfile=ami.json
            """)
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
            buildDescription('', "${site}-\${BUILD_ID}")
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

listView('Infrastructure') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        view.each {
            name(it.name)
        }
    }
    columns {
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
