import static build.Utils.repo
import static build.Utils.trim

class Site {

    String key

    String name

    String domain

    Site(String key, String name, String domain) {
        this.key = key
        this.name = name
        this.domain = domain
    }
}

def sites = [
    new Site('economic-action-plan', 'Economic Action Plan', 'economicactionplan.mygov.scot'),
    new Site('design-system-site', 'Design System', 'designsystem.mygov.scot'),
    new Site('resources', 'Resources', 'resources.mygov.scot'),
    new Site('trading-nation', 'Trading Nation', 'tradingnation.mygov.scot'),
]

def jobs = []

sites.each { site ->
    jobs << job(site.key) {
        displayName(site.name)

        logRotator {
            daysToKeep(60)
        }

        scm {
            git(repo(site.domain), 'master') {
                clean(true)
            }
        }

        triggers {
            scm('# Poll SCM enabled to allow trigger from git hook.')
        }

        steps {
            def script = StringBuilder.newInstance()
            script << "set -eux\n"
            script << "domain=${site.domain}\n"
            script << trim('''\
                ./build

                if [ "$FACTER_machine_env" = "services" ]; then
                  aws s3 sync --no-progress --delete _site/ s3://$domain
                fi
                '''.stripIndent())

            if (site.key != 'resources') {
            script << trim('''\
                git config remote.source.fetch +refs/*:refs/mirror/*
                git config remote.source.url \$(git config remote.origin.url)
                git fetch source
                git config remote.target.url git@github.com:scottishgovernment/${domain}.git
                git config remote.target.push refs/mirror/*:refs/*
                git push --mirror target
                '''.stripIndent())
            }
            shell(script.toString())
        }

        publishers {
            slackNotifier {
                baseUrl(null)
                commitInfoChoice('NONE')                    
                notifyAborted(true)
                notifyFailure(true)
                notifyNotBuilt(true)
                notifyUnstable(true)
                notifyBackToNormal(true)
                notifyRepeatedFailure(true)
            }
        }

    }
}

listView('Jekyll Sites') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        jobs.each {
            name(it.name)
        }
    }
    columns {
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
