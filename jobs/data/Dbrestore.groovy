package data

def build(site, List<String> envs) {
    dsl.job("dbrestore-${site.id}") {

        displayName("Restore data for ${site.domain}")

        parameters {
            choiceParam('env', envs, 'Environment to which production data is copied')
        }
        steps {
          def script = dsl.readFileFromWorkspace('resources/dbrestore')
          shell(script)
        }

        publishers {
            buildDescription('', '${env}')
        }
    }
}
