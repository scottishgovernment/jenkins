package data

def build(site, List<String> envs) {
    dsl.job("migration-${site.id}") {

        displayName("Run Hippo migrations on ${site.domain} environment")

        parameters {
            choiceParam('env', envs, "${site.domain} environment")
            choiceParam('host', ["pubapp01", "pubapp02"], "host to run on")
            choiceParam('migration', [
                'all',
                'default',
                'aps',
                'articles',
                'convert-links',
                'directorates',
                'groups',
                'home',
                'metadata',
                'orgroles',
                'people',
                'policies',
                'roles',
                'siteitems',
                'topics',
                'users',
            ], 'Select migration(s) to run. \nall: run all available migrations\ndefault: run any migrations not already run.')

            choiceParam('type', ['full', 'partial'],
                'Publications only\nfull: migrate all content \npartial: only migrate subset of the content (lastest 50)')

            stringParam('slugs', '', 'Publications only\nSpace separated list of slugs to migrate')
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
