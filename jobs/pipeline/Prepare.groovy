package pipeline

def build(site) {

    def id = site.id
    def test = site.environments.find { it.prepare }.name
    def artifact = site.id + '-site'

    dsl.job(id + '-release-prepare') {
        displayName("Prepare ${site.domain} release")
        logRotator {
            daysToKeep(365)
        }
        steps {
            shell("pipeline prepare:${test},scot.mygov.release,${artifact},\${BUILD_ID}")
        }
        properties {
             promotions {
                  promotion {
                       name("Default")
                       icon("star-blue")
                       conditions {
                            selfPromotion(false)
                       }
                  }
             }
        }
    }


}
