package test

import static build.Utils.repo
import static build.Utils.trim

def build(pipeline, site, envs) {
    def e2eSite = site == 'rubric' ? 'mygov' : site
    def suite = site == 'rubric' ? 'pubE2E' : 'webE2E'

    dsl.job("${pipeline}-e2e-${site}") {
        displayName("Site tests for ${site}")
        parameters {
            choiceParam('env', envs, 'Use this option to select test environment against which tests shall be executed')
            choiceParam('smoke_only', ['false', 'true'], 'Use this option to ONLY run smoke tests')
        }
        logRotator {
            daysToKeep(60)
        }
        scm {
            git(repo('beta-e2e'), 'master')
        }
        steps {
            shell(trim("""\
                checksum=node_modules/.npm
                if [ ! -e "\$checksum" ] || ! sha1sum --status -c "\$checksum" ; then
                  mkdir -p node_modules
                  npm prune &&
                      npm install &&
                      sha1sum package.json > "\$checksum"
                fi
                opts=
                if [ "\$smoke_only" = "true" ]; then
                  opts="\${opts} -k"
                fi
                ./run.sh -m single -s ${e2eSite} -t ${suite} -e "\$env" \$opts
                """))
        }
        publishers {
            buildDescription('', '$env', '', '$env')
            archiveJunit('reports/xml/*.xml')
            publishHtml {
                report("reports/e2e") {
                    reportName("HTML Report")
                    reportFiles("chrome-test-report.html")
                    allowMissing()
                    keepAll()
                    alwaysLinkToLastBuild()
                }
            }
        }
    }
}
