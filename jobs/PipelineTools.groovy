import static scot.mygov.jenkins.Utils.repo

job("Pipeline Tools") {
    scm {
        git(repo('deploy-pipeline'))
    }
    steps {
        shell(readFileFromWorkspace('resources/pipeline-tools-build.sh'))
    }
    properties {
         promotions {
              promotion {
                   name("Development")
                   icon("buildserver")
                   conditions {
                        selfPromotion()
                   }
                   actions {
                        shell(readFileFromWorkspace('resources/deploy-pipeline-tools.sh'))
                   }
              }
         }
    }
}
