package data

def build(site, List<String> envs) {
    dsl.job("migration-${site.id}") {

        displayName("Run Hippo migrations on ${site.domain} environment")

        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            choiceParam('migration', [
                'default',
                'all',
                'articles',
                'convert-links',
                'directorates',
                'groups',
                'orgroles',
                'people',
                'policies',
                'roles',
                'topics',
                'users',
            ], 'Select migration to run')
        }

        logRotator {
            daysToKeep(90)
        }

        steps {
            def script = StringBuilder.newInstance()
            script << 'ssh devops@${env}pubapp01.${env}.gov.scot \\\n'
            script << '  sudo su - migration -c \\"/opt/migration/run ${migration}\\"'
            shell(script.toString())
        }

    }
}
