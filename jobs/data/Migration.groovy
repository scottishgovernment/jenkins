package data

import static build.Utils.trim

def build(site, List<String> envs) {
    dsl.job("migration-${site.id}") {

        displayName("Run Hippo migrations on ${site.domain} environment")

        parameters {
            choiceParam('env', envs, "${site.domain} environment")

            booleanParam('clean', false, 'Delete existing data')

            booleanParam('background', true, 'Use false to hog a jenkins executor')

            choiceParam('host', ["pubapp01", "pubapp02"], "host to run on")

            choiceParam('migration', [
                'all',
                'users',
                'siteitems',
                'statuspages',
                'metadata',
                'topics',
                'issues',
                'people',
                'roles',
                'news',
                'publications',
                'historicalpublicationurls',
                'urlaliases',
                'directorates',
                'policies',
                'groups',
                'articles',
                'orgroles',
                'home',
                'landingpages',
                'topicrelationships',
                'issuerelationships',
                'convert-links',
                'node-order',
                'feedback'
            ], trim('''\
                Select migration(s) to run.
                all: run all available migrations.
            '''))

            choiceParam('includedisabled', ['', 'includedisabled'], 'Include disabled migrations')

            choiceParam('type', ['full', 'partial'], trim('''\
                Publications only
                full: migrate all content
                partial: only migrate subset of the content (latest 50)
            '''))

            stringParam('slugs', '', trim('''\
                Publications: space separated list of slugs to migrate
                Users: space separated list of users who should be admins
            '''))
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
