package data

import static build.Utils.trim

def build(site, List<String> envs) {
    return [
        migrate(site, envs),
        publications(site, envs),
        users(site, envs)
    ]
}

def migrate(site, envs) {
    dsl.job("migration-${site.id}") {

        displayName("Migrate content")

        parameters {
            choiceParam('env', envs, "${site.domain} environment")

            booleanParam('clean', false, 'Delete existing data')

            booleanParam('background', true, 'Use false to hog a jenkins executor')

            choiceParam('host', ["pubapp01", "pubapp02"], "host to run on")

            choiceParam('task', [
                'release',
                'test'
            ], trim('''\
                Select migration task to run.
                release : migrate all content required for a release,
                test    : migrate all content required for a test environment
            '''))

            booleanParam('publications', false, 'Include publications migration')
            booleanParam('feedback', false, 'Include feedback migration')
            booleanParam('historicalpublicationurls', false, 'Include historicalpublicationurls migration')
            booleanParam('urlaliases', false, 'Include urlaliases migration')
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


def publications(site, List<String> envs) {
    dsl.job("migration-publications-${site.id}") {

        displayName("Migrate publications")

        parameters {

            choiceParam('env', envs, "${site.domain} environment")

            booleanParam('clean', false, 'Delete existing data')

            booleanParam('background', true, 'Use false to hog a jenkins executor')

            choiceParam('host', ["pubapp01", "pubapp02"], "host to run on")

            stringParam('args', '', trim('''\
                    space separated list of publication slugs to migrate
                '''))
        }

        logRotator {
            daysToKeep(90)
        }

        steps {
            def script = dsl.readFileFromWorkspace('resources/migratePublications')
            shell(script)
        }

    }
}


def users(site, List<String> envs) {
    dsl.job("migration-users-${site.id}") {

        displayName("Migrate users")

        parameters {

            choiceParam('env', envs, "${site.domain} environment")

            booleanParam('clean', false, 'Delete existing data')

            booleanParam('background', true, 'Use false to hog a jenkins executor')

            choiceParam('host', ["pubapp01", "pubapp02"], "host to run on")

            stringParam('args', '', trim('''\
                Users: space separated list of users who should be admins
            '''))
        }

        logRotator {
            daysToKeep(90)
        }

        steps {
            def script = dsl.readFileFromWorkspace('resources/migrateUsers')
            shell(script)
        }

    }
}
