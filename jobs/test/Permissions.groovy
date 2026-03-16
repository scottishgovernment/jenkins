package test

def build(pipeline, envs) {
    def testEnvs = envs.take(2)
    def job = createJob(pipeline, testEnvs)
    return job
}

def createJob(pipeline, testEnvs) {
    def runScript = dsl.readFileFromWorkspace('resources/permissions-run')
            .replace('%pipeline%', pipeline)

    def resultsScript = dsl.readFileFromWorkspace('resources/permissions-results')
            .replace('%pipeline%', pipeline)

    dsl.pipelineJob("${pipeline}-permissions") {
        displayName("Permissions tests for ${pipeline}")

        parameters {
            choiceParam('testEnv', testEnvs, 'Test environment')
        }

        logRotator {
            daysToKeep(10)
        }

        definition {
            cps {
                script("""\
                    pipeline {
                        agent none
                        stages {
                            stage('Run tests') {
                                agent any
                                steps {
                                    sh '''${runScript}'''
                                }
                            }
                            stage('Pause') {
                                steps {
                                    sleep time: 2, unit: 'MINUTES'
                                }
                            }
                            stage('Get results') {
                                agent any
                                steps {
                                    sh '''${resultsScript}'''
                                }
                                post {
                                    always {
                                        junit testResults: 'test-results.xml', allowEmptyResults: false
                                    }
                                }
                            }
                        }
                    }
                """.stripIndent())
                sandbox()
            }
        }
    }
}
