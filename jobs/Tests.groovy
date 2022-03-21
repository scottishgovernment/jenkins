import static build.Utils.repo
import static build.Utils.trim

def jobs = []

jobs << job('accessibility-tests') {
    displayName('Accessibility Tests')
    parameters {
        choiceParam('environment', ['dev', 'int', 'exp', 'per', 'uat', 'tst', 'blu', 'grn', 'dgv', 'igv', 'egv', 'pgv', 'ugv', 'tgv', 'bgv', 'ggv'], 'Envirnoment to run the tests')
        choiceParam('website', ['mygov','gov'], 'Target Website to Validate')
        choiceParam('standard', ['WCAG2AA', 'Section508', 'WCAG2A', 'WCAG2AAA'], 'Accessibility Standard')
        choiceParam('keyword', ['error', 'warning', 'notice'], 'Keyword to signify errors')
    }
    logRotator {
        daysToKeep(60)
    }
    scm {
        git(repo('beta-website-accessibility-tests'), 'master')
    }
    steps {
        shell(trim('''\
            ./run.sh -e ${environment} -w ${website} -s ${standard} -k ${keyword}
        '''))
    }
    publishers {
        buildDescription('', '$website', '', '$website')
        archiveJunit('logs/**/xml/*.xml')
        publishHtml {
             report("logs/mygov/html") {
                  reportName("MyGov Homepage Report")
                  reportFiles("home-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Contact us Report")
                  reportFiles("contact-us-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Search page Report")
                  reportFiles("search-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Guide page Report")
                  reportFiles("guide-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Organisations page Report")
                  reportFiles("organisations-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov page with dropdown Report")
                  reportFiles("dropdown-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/mygov/html") {
                  reportName("MyGov Corporate Org Hub Report")
                  reportFiles("corp-org-hub-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }

             report("logs/mygov/html") {
                  reportName("MyGov Brexit From Report")
                  reportFiles("brexit-form.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }

            report("logs/mygov/html") {
                  reportName("MyGov Disclosure From Report")
                  reportFiles("disclosure-form.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }

            report("logs/mygov/html") {
                  reportName("MyGov PVG From Report")
                  reportFiles("pvg-form.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }

             report("logs/gov/html") {
                  reportName("Gov Home page Report")
                  reportFiles("home-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov News landing page Report")
                  reportFiles("news-landing-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov News Item page Report")
                  reportFiles("news-item.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Policies landing page Report")
                  reportFiles("policies-landing-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Publications landing page Report")
                  reportFiles("publications-landing-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Non-APS Publication page Report")
                  reportFiles("publication-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov APS Publication page Report")
                  reportFiles("aps-publication-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Directorates list Report")
                  reportFiles("directorates-atozlist.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Directorate page Report")
                  reportFiles("directorate-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Cabinet and Ministers page Report")
                  reportFiles("cabinet-and-ministers.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Civil Service page Report")
                  reportFiles("civil-service.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Person page Report")
                  reportFiles("person-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Featured Role page Report")
                  reportFiles("featured-role-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Role page Report")
                  reportFiles("role-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Topic page Report")
                  reportFiles("topic-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Issue Hub page Report")
                  reportFiles("issue-hub-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/gov/html") {
                  reportName("Gov Non APS Page Report")
                  reportFiles("non-aps-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("logs/beta2/html") {
                  reportName("Beta2 Home page Report")
                  reportFiles("home-page.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }

        }
    }
}

jobs << job('rubric-api-tests') {
    displayName('Rubric API tests')
    parameters {
        choiceParam('TESTENV', ['int', 'exp', 'per', 'uat', 'tst', 'igv', 'egv', 'pgv', 'ugv', 'tgv'], 'Use this option to select test environment against which tests shall be executed')
        choiceParam('groups', ['govsmoke', 'mygovsmoke','govregression','mygovregression'], 'Target Website to Validate')
    }
    logRotator {
        daysToKeep(60)
    }
    scm {
        git(repo('rubric-api-tests'), 'master')
    }
    steps {
        shell(trim('''\
            ./run.sh -e ${TESTENV} -g ${groups}
        '''))
    }
    publishers {
        archiveJunit('target/surefire-reports/junitreports/*.xml')
        publishHtml {
             report("target/surefire-reports/") {
                  reportName("Rubric API Test Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
             }
        }
    }
}

jobs << job('end-to-end-tests') {
    displayName('End-to-end tests')
    parameters {
        choiceParam('site', ["mygov", "gov", "tradingnation"], 'use this option to select tests for mygov.scot, gov.scot or trading nation')
        choiceParam('testenv', ["int", "exp", "per", "uat", "tst", "igv", "egv", "pgv", "ugv", "tgv"], 'Use this option to select test environment against which tests shall be executed')
        choiceParam('mode', ['single', 'multi'], 'Use this option to run the tests only in Chrome (single) or on Chrome, Firefox and Safari (multi)')
        choiceParam('smoke_only', ['false', 'true'], 'Use this option to ONLY run smoke tests')
        stringParam('tests', 'webE2E', 'Use this option to specify what tests to run. Enter a comma-separated (NO SPACES) list with any combination of these values: webE2E,pubE2E')
    }
    logRotator {
        daysToKeep(60)
    }
    scm {
        git(repo('beta-e2e'), 'master')
    }
    steps {
        shell(trim('''\
            if [ "\$smoke_only" = "true" ]; then
                ./run.sh -s ${site} -m ${mode} -t ${tests} -e ${testenv} -k
            else
                ./run.sh -s ${site} -m ${mode} -t ${tests} -e ${testenv}
            fi
        '''))
    }
    publishers {
        buildDescription('', '$site - $testenv -$tests', '', '$site - $testenv - $tests')
        archiveJunit('reports/xml/*.xml')
        publishHtml {
             report("reports/e2e") {
                  reportName("MyGov Site HTML Report")
                  reportFiles("chrome-test-report.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("reports/e2e") {
                  reportName("Gov Site HTML Report")
                  reportFiles("chrome-test-report.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
        }
    }
}

jobs << job('layout-tests') {
    displayName('Layout Tests')
    parameters {
        choiceParam('site', ['mygov', 'gov'], 'Site to tests, either MyGov.scot or Gov.scot')
        choiceParam('target_platform', ['www', 'pub'], 'To run the tests either on Informational Website or Publishing Platform')
        choiceParam('test_env', ['int', 'dev', 'exp', 'per', 'uat', 'tst', 'blu', 'grn', 'dgv', 'igv', 'egv', 'pgv', 'ugv', 'tgv', 'bgv', 'ggv'], 'The test environment to be used')
        choiceParam('browser', ['chrome', 'firefox', 'all'], 'Browser to test')
        stringParam('webdriver_ip', '10.21.134.66', 'Use this option to specify the IP address of the machine running Selenium web driver')
        stringParam('groups', '', 'Leave empty for all. MyGov - articlepage, corporghubpage, doccollectionpage, guidepage, homepage, orglistpage, searchpage; Gov - apspage, cabinetandministerspage, civilservicepage, directoratepage, featuredrolepage, grouppage, homepage, issuehubpage, newspage, nonapspage, policypage, publicationspage, rolepage, searchpage, topicpage, topicspage')
    }
    logRotator {
        daysToKeep(60)
    }
    scm {
        git(repo('beta-layout-tests'), 'master') {
            clean(true)
        }
    }
    steps {
        shell(trim('''\
            ./run.sh -s ${site} -t ${target_platform} -e ${test_env} -b ${browser} ${groups:+-g ${groups}} ${webdriver_ip:+-i ${webdriver_ip}}
        '''))
    }

    publishers {
        buildDescription('', '$site $target_platform - $test_env', '', '$site $target_platform - $test_env')
        archiveTestNG('reports/**/xml/*.xml'){
          showFailedBuildsInTrendGraph()
          markBuildAsFailureOnFailedConfiguration()
        }
        publishHtml {
             report("reports/www/mygov/html") {
                  reportName("MyGov Site HTML Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
             report("reports/www/gov/html") {
                  reportName("Gov Site HTML Report")
                  reportFiles("index.html")
                  allowMissing()
                  keepAll()
                  alwaysLinkToLastBuild()
             }
        }
    }
}

jobs << job('security-tests') {
    displayName('Security Tests')
     parameters {
        choiceParam('test_env', ['int', 'dev', 'exp', 'per', 'dgv', 'igv', 'egv', 'pgv'], 'The test environment to be used')
    }
    logRotator {
        daysToKeep(60)
    }
    scm {
        git(repo('beta-security-tests'))
    }
    steps {
        shell(trim('''\
            cd SSL-Automated-Security-Tests
            ./AutomatedSSLSecurityTests.sh -e ${test_env}
        '''))
    }
}

jobs << pipelineJob('integration-test-mygov') {
    displayName('Integration Test Mygov')
    logRotator {
        daysToKeep(14)
    }
    definition {
        cps {
            def pipeline = StringBuilder.newInstance()
            pipeline << """

            stage('Promote') {
                build job: 'promote-mygov', parameters: [
                    string(name: 'from', value: 'dev'),
                    string(name: 'to', value: 'int')
                ]
            }

            stage('Build') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'mygov-int', parameters: [
                        string(name: 'action', value: 'build')
                    ]
                }
            }

            stage('Pause') {
                sleep time: 15, unit: 'MINUTES'
            }

            stage('Migrations') {
                build job: 'migration-mygov', parameters: [
                    string(name: 'env', value: 'int'),
                    booleanParam(name: 'background', value: false),
                    string(name: 'host', value: 'pubapp01')
                ]
            }

            stage('mygov-perceptual') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'mygov-perceptual-tests', parameters: [
                        string(name: 'testEnv', value: 'int'),
                        string(name: 'referenceEnv', value: 'live')
                    ]
                }
            }

            stage('mygov-pube2e') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'mygov-e2e-rubric',
                    parameters: [
                        string(name: 'env', value: 'int'),
                        string(name: 'smoke_only', value: 'true')
                    ]
                }
            }

            stage('mygov-webe2e') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'mygov-e2e-mygov', parameters: [
                        string(name: 'env', value: 'int'),
                        string(name: 'smoke_only', value: 'false')
                    ]
                }
            }

            stage('tradingnation-webe2e') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'mygov-e2e-tradingnation', parameters: [
                        string(name: 'env', value: 'int'),
                        string(name: 'smoke_only', value: 'false')
                    ]
                }
            }

            stage('teardown int') {
                build job: 'mygov-int', parameters: [
                    string(name: 'action', value: 'teardown')
                ]
            }

            """.stripIndent()
            script(pipeline.toString())
            sandbox()
        }
    }
}

jobs << pipelineJob('integration-test-gov') {
    displayName('Integration Test Gov')
    logRotator {
        daysToKeep(14)
    }
    definition {
        cps {
            def pipeline = StringBuilder.newInstance()
            pipeline << """

            stage('Promote') {
                build job: 'promote-gov', parameters: [
                    string(name: 'from', value: 'dgv'),
                    string(name: 'to', value: 'igv')
                ]
            }

            stage('Build') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'gov-igv', parameters: [
                        string(name: 'action', value: 'build')
                    ]
                }
            }

            stage('Pause') {
                sleep time: 15, unit: 'MINUTES'
            }

            stage('Migrations') {
                build job: 'migration-gov', parameters: [
                    string(name: 'env', value: 'igv'),
                    booleanParam(name: 'background', value: false),
                    string(name: 'host', value: 'pubapp01')
                ]
            }

            stage('gov-perceptual') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'gov-perceptual-tests', parameters: [
                        string(name: 'testEnv', value: 'igv'),
                        string(name: 'referenceEnv', value: 'live')
                    ]
                }
            }
            
            stage('gov-webe2e') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'gov-e2e-gov', parameters: [
                        string(name: 'env', value: 'igv'),
                        string(name: 'smoke_only', value: 'false')
                    ]
                }
            }

            stage('teardown igv') {
                build job: 'gov-igv', parameters: [
                    string(name: 'action', value: 'teardown')
                ]
            }

            """.stripIndent()
            script(pipeline.toString())
            sandbox()
        }
    }
}

jobs << pipelineJob('build-egv-environment') {
    displayName('Setup Egv Environment')
    logRotator {
        daysToKeep(7)
    }
    definition {
        cps {
            def pipeline = StringBuilder.newInstance()
            pipeline << """

            stage('Promote') {
                build job: 'promote-gov', parameters: [
                    string(name: 'from', value: 'igv'),
                    string(name: 'to', value: 'egv')
                ]
            }

            stage('Build') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'gov-egv', parameters: [
                        string(name: 'action', value: 'build')
                    ]
                }
            }

            stage('Pause') {
                sleep time: 15, unit: 'MINUTES'
            }

            stage('Migrations') {
                build job: 'migration-gov', parameters: [
                    string(name: 'env', value: 'egv'),
                    booleanParam(name: 'background', value: false),
                    string(name: 'host', value: 'pubapp01')
                ]
            }

            """.stripIndent()
            script(pipeline.toString())
            sandbox()
        }
    }
}

jobs << pipelineJob('build-exp-environment') {
    displayName('Setup Exp Environment')
    logRotator {
        daysToKeep(7)
    }
    definition {
        cps {
            def pipeline = StringBuilder.newInstance()
            pipeline << """

            stage('Promote') {
                build job: 'promote-mygov', parameters: [
                    string(name: 'from', value: 'int'),
                    string(name: 'to', value: 'exp')
                ]
            }

            stage('Build') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'mygov-exp', parameters: [
                        string(name: 'action', value: 'build')
                    ]
                }
            }

            stage('Pause') {
                sleep time: 15, unit: 'MINUTES'
            }

            stage('Migrations') {
                build job: 'migration-mygov', parameters: [
                    string(name: 'env', value: 'exp'),
                    booleanParam(name: 'background', value: false),
                    string(name: 'host', value: 'pubapp01')
                ]
            }

            """.stripIndent()
            script(pipeline.toString())
            sandbox()
        }
    }
}

listView('Test') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        jobs.each {
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
