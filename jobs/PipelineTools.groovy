import static scot.mygov.jenkins.Utils.repo

job('pipeline') {
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
        shell(readFileFromWorkspace('resources/pipeline-build.sh'))
    }
    properties {
         promotions {
              promotion {
                   name("Build Server")
                   icon("star-blue")
                   conditions {
                        selfPromotion()
                   }
                   actions {
                        shell(readFileFromWorkspace('resources/pipeline-deploy.sh'))
                   }
              }
         }
    }
}
