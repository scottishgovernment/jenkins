package pipeline

def build(site) {
    def id = site.id
    def domain = site.domain
    def prod = site.environments.
        findAll { it.perform }.
        collect { it.name }
    def artifact = id + '-site'

    dsl.job(id + '-release-perform') {
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
