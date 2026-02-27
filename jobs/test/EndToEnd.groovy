package test

import static build.Utils.repo
import static build.Utils.trim

def build(pipeline, site, envs) {
    def e2eSite = site

    dsl.job("${pipeline}-e2e-${site}") {
        displayName("Site tests for ${site}")
        parameters {
            choiceParam('env', envs, 'Use this option to select test environment against which tests shall be executed')
            choiceParam('suite', ['webE2E', 'housing', 'feedback', 'mygovSearch', 'govSearch'], 'Use this option to select the test suite to be ran')
        }
        logRotator {
            daysToKeep(60)
        }
        scm {
            git(repo('end-to-end-tests'), 'main')
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
                ./run.sh -m jenkins -s ${e2eSite} -t "\$suite" -e "\$env"
                """))
        }
        publishers {
            buildDescription('', '$env', '', '$env')
            archiveJunit('reports/junit/results-*.xml')
            publishHtml {
                report("reports/html-reports/") {
                    reportName("HTML Report")
                    reportFiles("master-report.html")
                    allowMissing()
                    keepAll()
                    alwaysLinkToLastBuild()
                }
            }
        }
    }
}
