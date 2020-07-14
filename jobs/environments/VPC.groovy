package environments

import static build.Utils.awsRepo

def build(site) {
    def list = []
    def environments = site.environments

    environments.eachWithIndex { environment, i ->
        def type = environment.type
        list << createJob(site, type, environment.name, i + 1)
    }
    list
}

def createJob(site, type, env, index) {

    return dsl.job("${site.id}-${env}") {
        displayName("${index}. ${env} environment")
        parameters {
            choiceParam('action', ['build', 'teardown', 'rebuild'],
                "The operation to be performed.")
            stringParam('ami_override', '',
               "If the required version isn't available above, specify it here.")
        }
        logRotator {
          daysToKeep(90)
        }
        steps {
            def script = dsl.readFileFromWorkspace('resources/vpcctl').
                replace('%id%', site.id).
                replace('%domain%', site.domain).
                replace('%build%', site.types.get(type).up).
                replace('%teardown%', site.types.get(type).down).
                replace('%env%', env)
            shell(script)
        }
        publishers {
            buildDescription('', '$action')
        }
        configure {
            def params = (it / 'properties'
                / 'hudson.model.ParametersDefinitionProperty'
                / 'parameterDefinitions')
                .children()

            params.add(1, 'hudson.plugins.promoted__builds.parameters.PromotedBuildParameterDefinition' {
                name('ami')
                description('')
                projectName("${site.id}-ami")
                promotionProcessName('Default')
            })
        }
    }
}
