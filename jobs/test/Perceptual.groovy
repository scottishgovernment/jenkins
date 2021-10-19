package test

import static build.Utils.repo
import static build.Utils.trim

def build(pipeline, envs) {

    dsl.job("${pipeline}-perceptual-tests") {
        displayName("Perceptual tests for ${pipeline}")

        def testEnvs = envs + ['live']
        def referenceEnvs = ['live'] + envs

        parameters {
            choiceParam('testEnv', testEnvs, 'Test environment')
            choiceParam('referenceEnv', referenceEnvs, 'Reference environment')
        }
        logRotator {
            daysToKeep(10)
        }
        scm {
            git(repo('perceptual-testing'), 'master')
        }
        steps {
            shell(trim("""\
                ./run.sh -s ${pipeline} -r \${referenceEnv} -t \${testEnv}
            """))
        }
        publishers {
            buildDescription('', '$testEnv vs $referenceEnv', '', '$testEnv vs $referenceEnv')
            archiveJunit('backstop_data/**/ci_report/*.xml')
            publishHtml {
                report("backstop_data/www/${pipeline}") {
                    reportName("Big resolutions report")
                    reportFiles("html_report/big_res/index.html")
                    allowMissing()
                    alwaysLinkToLastBuild()
                    keepAll()
                }
                report("backstop_data/www/${pipeline}") {
                    reportName("Small resolutions report")
                    reportFiles("html_report/small_res/index.html")
                    allowMissing()
                    alwaysLinkToLastBuild()
                    keepAll()
                }
            }
        }
    }

}
