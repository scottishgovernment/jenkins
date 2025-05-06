package data

import static build.Utils.awsRepo

def build(site, List<String> envs) {
    dsl.job("checker-${site.id}") {

        displayName("Run repository maintenance on ${site.domain}")

        parameters {
            choiceParam('env', envs, 'Environment to run maintenance on')
            choiceParam('type', [
                "clean",
                "cleanvh",
                "cleands"
            ], "The type of maintenance to perform")
            booleanParam('stop', true, 'Stop Hippo while running maintenance')
            choiceParam('host', ["pubapp01", "pubapp02"], "host to run on")
        }

        steps {
            def template = dsl.readFileFromWorkspace('resources/checker')
            def script = template.replaceFirst("\n", "\ndomain=${site.domain}\n")
            shell(script.toString())
        }

        publishers {
            buildDescription('', '${env}')
        }
    }
}
