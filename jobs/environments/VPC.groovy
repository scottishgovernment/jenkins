package environments

import javaposse.jobdsl.dsl.DslFactory

import static build.Utils.trim
import static build.Utils.awsRepo

class VPC {

    DslFactory dsl

    PrintStream out

    VPC(dsl, out) {
        this.dsl = dsl
        this.out = out
    }

    def site(site) {
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
        def cmds = StringBuilder.newInstance()
        cmds << "#!/bin/sh -e\n"
        cmds << "ami=\${override:-\$version_NUMBER}\n\n"
        cmds << "tools/management/s3_restore ${site.domain} \${env}\n"
        cmds << site.types.get(type).up << '\n'

        return dsl.job("${site.id}-${type}-up") {
            displayName("Build ${site.domain} ${type} environment")
            scm {
                awsRepo(delegate)
            }
            parameters {
                choiceParam('env', envs, "${site.domain} environment")
            }
            steps {
                shell(cmds.toString())
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
        def script = site.types.get(type).down

        return dsl.job("${site.id}-${type}-down") {
            displayName("Tear down ${site.domain} ${type} environment")
            scm {
                awsRepo(delegate)
            }
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

}
