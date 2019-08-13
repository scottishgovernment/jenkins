package data

import static build.Utils.trim

def build(sites) {
  return sites.collect { site ->
    envs = site.environments.collect { it.name }
    migrate(site, envs)
  }
}

def migrate(site, envs) {
    dsl.job("migration-${site.id}") {

        displayName("Migrate ${site.id} content")

        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            booleanParam('background', true, 'Use false to hog a jenkins executor')
            choiceParam('host', ['pubapp01', 'pubapp02'], 'host to run on')
        }

        logRotator {
            daysToKeep(90)
        }

        steps {
            def scriptBuilder = StringBuilder.newInstance()
            scriptBuilder << dsl.readFileFromWorkspace('resources/migrate')
            scriptBuilder << dsl.readFileFromWorkspace('resources/migrate-logs')
            def script = scriptBuilder.toString();
            script = script.replace('<site>', site.id);
            shell(script);
        }

    }
}
