package environments

import static build.Utils.awsRepo

def build(site) {
    def list = []
    def environments = site.environments
    def types = environments.collect { it.type }.unique(false)
    types.collect { type ->
        def envs = environments.
            grep { it.type == type }.
            collect { it.name }
        if (site.types.get(type)?.up) {
            list << envUp(site, type, envs)
        }
        if (site.types.get(type)?.down) {
            list << envDown(site, type, envs)
        }
    }
    list
}

def envUp(site, type, List<String> envs) {
    def script = dsl.readFileFromWorkspace('resources/vpc-up.sh').
        replace('%domain%', site.domain).
        replace('%build%', site.types.get(type).up)

    return dsl.job("${site.id}-${type}-up") {
        displayName("Build ${site.domain} ${type} environment")
        parameters {
            choiceParam('env', envs, "${site.domain} environment")
        }
        steps {
            shell(script)
        }
        publishers {
            buildDescription('', '${env}')
        }
        parameters {
            stringParam('override', '',
                "If the required version isn't available above, specify it here.")
        }
        configure {
            def params = (it / 'properties'
                / 'hudson.model.ParametersDefinitionProperty'
                / 'parameterDefinitions')
                .children()

            params.add(0, 'hudson.plugins.promoted__builds.parameters.PromotedBuildParameterDefinition' {
                name('version')
                description('')
                projectName("${site.id}-ami")
                promotionProcessName('Default')
            })
        }
    }
}

def envDown(site, type, List<String> envs) {
    def script = dsl.readFileFromWorkspace('resources/vpc-down.sh').
        replace('%teardown%', site.types.get(type).down)

    return dsl.job("${site.id}-${type}-down") {
        displayName("Tear down ${site.domain} ${type} environment")
        parameters {
            choiceParam('env', envs, "${site.domain} environment")
        }
        steps {
            shell(script)
        }
        publishers {
            buildDescription('', '${env}')
        }
    }
}
