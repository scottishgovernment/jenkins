import static scot.mygov.jenkins.Utils.repo

sites = [
    [
        'id': 'mygov',
        'name': 'mygov.scot',
        'artifact': 'mygov-site',
        'test': 'per',
        'prod': ['blu', 'grn']

    ],
    [
        'id': 'gov',
        'name': 'gov.scot',
        'artifact': 'gov-site',
        'test': 'pgv',
        'prod': ['bgv', 'ggv']
    ],
]

sites.each { site ->

    def id = site.id
    def domain = site.name
    def test = site.test
    def prod = site.prod
    def artifact = site.artifact

    job(id + '-release-prepare') {
        displayName("Prepare ${domain} release")
        steps {
            shell("pipeline prepare:${test},scot.mygov.release,${artifact},\${BUILD_ID}")
        }
        properties {
             promotions {
                  promotion {
                       name("Default")
                       icon("star-blue")
                       conditions {
                            selfPromotion()
                       }
                  }
             }
        }
    }

    job(id + '-release-perform') {
        displayName("Perform ${domain} release")

        parameters {
            choiceParam('env', prod, "${domain} production environment")
            stringParam('override', '',
                "If the required version isn't available above, specify it here.")
        }

        steps {
            shell("pipeline perform:\${env},scot.mygov.release,${artifact},\${override:-\$version_NUMBER} sync")
        }

        publishers {
            buildDescription('', '${env} - ${version.number}')
        }

        configure {
            params = (it / 'properties'
                / 'hudson.model.ParametersDefinitionProperty'
                / 'parameterDefinitions')
                .children()

            params.add(0, 'hudson.plugins.promoted__builds.parameters.PromotedBuildParameterDefinition' {
                name('version')
                description('')
                projectName("${id}-release-prepare")
                promotionProcessName('Default')
            })

        }

    }

}
