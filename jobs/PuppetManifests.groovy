import static scot.mygov.jenkins.Utils.repo

job("Puppet Manifests") {
    scm {
        git(repo('www-infrastructure'))
    }
    steps {
        shell(readFileFromWorkspace('resources/build-puppetry.sh'))
    }
    properties {
         promotions {
              promotion {
                   name("Development")
                   icon("Gold Star")
                   conditions {
                        selfPromotion()
                   }
                   actions {
                        shell(readFileFromWorkspace('resources/deploy-puppetry.sh'))
                   }
              }
         }
    }
}
