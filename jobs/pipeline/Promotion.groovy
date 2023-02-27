package pipeline

def build(site, List<String> envs) {
    dsl.job("promote-${site.id}") {
        displayName("Promote ${site.domain}")

        logRotator {
            daysToKeep(90)
        }

        parameters {
            choiceParam('from', envs, 'Get versions from this environment')
            choiceParam('to',   envs.drop(1), 'Stage versions in this environment')
        }

        steps {
            shell('pipeline promote:${from},${to} sync')
        }

        publishers {
            buildDescription('', '${from} - ${to}')
        }

    }
}
