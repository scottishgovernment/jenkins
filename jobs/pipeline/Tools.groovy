package pipeline

import static build.Utils.repo


def build(dsl) {
    dsl.job('pipeline') {
        displayName('Pipeline')
        scm {
            git {
                remote {
                    name('deploy-pipeline')
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
                            shell("pipeline deploy:pipeline,${version},build sync")
                            shell(dsl.readFileFromWorkspace('resources/pipeline-deploy.sh'))
                       }
                  }
             }
        }
    }
    dsl.job('repo-clean') {
        displayName('Repo Clean')
        scm {
            git {
                remote {
                    name('repo-clean')
                    url(repo('repo-clean'))
                }
                branch('refs/heads/master')
            }
        }
        steps {
            shell(dsl.readFileFromWorkspace('resources/repo-clean-build.sh'))
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
                            shell("pipeline deploy:repo-clean,${version},build sync")
                            shell(dsl.readFileFromWorkspace('resources/repo-clean-deploy.sh'))
                       }
                  }
             }
        }
    }
}
