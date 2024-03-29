package pipeline

import static build.Utils.repo


def build(dsl) {
    dsl.job('pipeline') {
        displayName('Pipeline')
        scm {
            git {
                remote {
                    name('origin')
                    url(repo('deploy-pipeline'))
                }
                branch('refs/heads/master')
            }
        }
        steps {
            shell(dsl.readFileFromWorkspace('resources/pipeline-build.sh'))
        }
        triggers {
            scm('# Poll SCM enabled to allow trigger from git hook.')
        }
        properties {
             promotions {
                  promotion {
                       name("Build Server")
                       icon("star-blue")
                       conditions {
                            selfPromotion(false)
                       }
                       actions {
                            String version = '1.0.${PROMOTED_ID}'
                            shell("""\
                                pipeline deploy:pipeline,${version},local
                                pipeline promote:local,services,pipeline
                                pipeline sync
                            """.stripIndent())
                            shell(dsl.readFileFromWorkspace('resources/pipeline-deploy.sh'))
                       }
                  }
             }
        }
    }
    dsl.job('repo-clean-build') {
        displayName('Repo Clean - Build')
        scm {
            git {
                remote {
                    name('origin')
                    url(repo('repo-clean'))
                }
                branch('refs/heads/master')
            }
        }
        steps {
            String mirror = 'git@github.com:scottishgovernment/repo-clean.git'
            shell(dsl.readFileFromWorkspace('resources/repo-clean-build'))
            shell("""\
                        git config remote.target.url ${mirror}
                        git push --tags --prune target +refs/remotes/origin/*:refs/heads/*
                    """.stripIndent())
        }
        triggers {
            scm('# Poll SCM enabled to allow trigger from git hook.')
        }
        properties {
             promotions {
                  promotion {
                       name("Build Server")
                       icon("star-blue")
                       conditions {
                            selfPromotion(false)
                       }
                       actions {
                            String version = '1.0.${PROMOTED_ID}'
                            shell("""\
                                pipeline deploy:repo-clean,${version},local
                                pipeline promote:local,services,repo-clean
                                pipeline sync
                            """.stripIndent())
                            shell(dsl.readFileFromWorkspace('resources/repo-clean-deploy'))
                       }
                  }
             }
        }
    }
    dsl.job('repo-clean-run') {
        displayName('Repo Clean - Run')
        parameters {
          choiceParam('runtype', ['dry-run', 'run'], 'Dry-Run or actual Run')
        }
        logRotator {
          daysToKeep(90)
        }
        steps {
            shell(dsl.readFileFromWorkspace('resources/repo-clean-run'))
        }
        publishers {
            buildDescription('', '${runtype}')
        }
    }
}
