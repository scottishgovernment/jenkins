package data

import static build.Utils.trim

def build(site) {

    def envs = site.environments
            .findAll{it.perform == null & it.prepare == null}
            .collect{it.name}


    dsl.job("specific-migration-${site.id}") {

        displayName("Run Specific Migration on ${site.id} content")

        logRotator {
            daysToKeep(90)
        }

        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            booleanParam('background', true, 'Use false to hog a jenkins executor')
            choiceParam('host', ['pubapp01', 'pubapp02'], 'host to run on')
            stringParam('migrations', '',
                    "Enter the specific migration you wish to run (case sensitive) eg. Mygov")
        }

        steps {
            // build the migration script with site variable defined
            def script = StringBuilder.newInstance()
            script << "#!/bin/sh -eu\n"
            script << "site=${site.id}\n"
            script << dsl.readFileFromWorkspace('resources/migrate')
            shell(script.toString());
        }
                
        publishers {
            buildDescription('', '${migrations} migration on ${env}')
        }
    }
}
