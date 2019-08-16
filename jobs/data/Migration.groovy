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
            // build the migration script with site variable defined
            def script = StringBuilder.newInstance()
            script << "#!/bin/sh -eu\n"
            script << "site=${site.id}\n"
            script << dsl.readFileFromWorkspace('resources/migrate')
            shell(script.toString());
        }
    }
}
