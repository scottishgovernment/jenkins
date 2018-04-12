package data

def build(site, List<String> envs) {
    dsl.job("migration-${site.id}") {

        displayName("Run Hippo migrations on ${site.domain} environment")

        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            choiceParam('host', ["pubapp01", "pubapp02"], "host to run on")
            choiceParam('migration', [
                'default',
                'all',
                'aps',
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

            choiceParam('type', ['full', 'partial'], 'Select migration type')

            stringParam('slugs', '', 'Specific publication slugs')
        }

        logRotator {
            daysToKeep(90)
        }

        steps {
            def script = dsl.readFileFromWorkspace('resources/migrate')
            shell(script)
        }

    }
}
