import static build.Utils.repo
import static build.Utils.trim

def jobs = []

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

            stage('mygov-end-to-end') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'mygov-e2e-mygov', parameters: [
                        string(name: 'env', value: 'int'),
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
            
            stage('gov-end-to-end') {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    build job: 'gov-e2e-gov', parameters: [
                        string(name: 'env', value: 'igv'),
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
