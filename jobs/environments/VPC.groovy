package environments

import static build.Utils.awsRepo

def build(site) {
    def list = []
    def environments = site.environments
    def types = environments.collect { it.type }.unique(false)

    environments.collect { environment -> 
        def type = environment.type
        def envListOrder = environment.number
        list << createJob(site, type, environment.name, envListOrder)
    }
    list
}

def createJob(site, type, env, envListOrder) {

    return dsl.job("${env}-${site.id}-${type}") {
        displayName("${envListOrder}. ${env} environment")
        parameters {
            choiceParam('operation', ['build', 'teardown', 'rebuild'],
                "The operation to be performed.")
            stringParam('ami_override', '',
               "If the required version isn't available above, specify it here.")
        }
        logRotator {
          daysToKeep(90)
        }
        steps {
            def buildScript = dsl.readFileFromWorkspace('resources/vpc-up.sh').
                replace('%id%', site.id).
                replace('%domain%', site.domain).
                replace('%build%', site.types.get(type).up).
                replace('%env%', env)
            def teardownScript = dsl.readFileFromWorkspace('resources/vpc-down.sh').
                replace('%id%', site.id).
                replace('%domain%', site.domain).
                replace('%build%', site.types.get(type).down).
                replace('%env%', env)

            shell("""
                |if [ "\$operation" = "build" ]; then
                    |${buildScript}
                |elif [ "\$operation" = "teardown" ]; then
                    |${teardownScript}
                |elif [ "\$operation" = "rebuild" ]; then 
                    |${teardownScript}
                    |${buildScript}
                |fi
            """.stripMargin().trim())
        }
        publishers {
            buildDescription('', '$operation')
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
