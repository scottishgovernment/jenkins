import static build.Utils.repo
import static build.Utils.trim
import static build.Utils.awsRepo

import org.yaml.snakeyaml.Yaml

def view = []
yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
sites = yaml.sites

view << job('blue-green-switch') {
    def script = StringBuilder.newInstance()
    script << readFileFromWorkspace('resources/aws') << '\n'
    script << 'tools/switch-nagios ${env}\n'
    script << 'tools/mygov-switch ${env}\n'

    displayName('Blue-Green Switch')

    logRotator {
        daysToKeep(365)
    }

    parameters {
        choiceParam('env', ['blu', 'grn'], 'mygov.scot production environment')
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(script.toString())
    }
    publishers {
        buildDescription('', '$env')
    }
}

view << job('bgv-ggv-govscot-switch') {
    def script = StringBuilder.newInstance()
    script << readFileFromWorkspace('resources/aws') << '\n'
    script << 'tools/switch-nagios ${env}\n'
    script << 'tools/gov-switch ${env}\n'

    displayName('Blue-Green Switch Gov.scot')

    logRotator {
        daysToKeep(365)
    }

    parameters {
        choiceParam('env', ['bgv', 'ggv'], 'gov.scot production environment')
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(script.toString())
    }
    publishers {
        buildDescription('', '$env')
    }
}

view << job('mygov-site-fail-trigger') {
    siteName = "mygov"
    site = sites.grep { it.id == siteName }.first()
    envNames = site.environments.collect { it.name } + [ siteName ]
    displayName('MyGov Site Fail Trigger')
    parameters {
        choiceParam('env', envNames, trim('''\
            Your chosen Environment will be redirected to a holding page.
            WARNING MyGov option will redirect the live site!'''))
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(trim('''\
            cd tools/management/
            ./mygov_sitefail_trigger ${env}
        '''))
    }
}

view << job('gov-site-fail-trigger') {
    siteName = "gov"
    site = sites.grep { it.id == siteName }.first()
    envNames = site.environments.collect { it.name } + [ siteName ]
    displayName('Gov Site Fail Trigger')
    parameters {
        choiceParam('env', envNames, trim('''\
            Your chosen Environment will be redirected to a holding page.
            WARNING Gov option will redirect the live site!'''))
    }
      scm {
          awsRepo(delegate)
      }
      steps {
          shell(trim('''\
              cd tools/management/
              ./gov_sitefail_trigger ${env}
          '''))
      }
}


view << job('mygov-site-fail-recover') {
    siteName = "mygov"
    site = sites.grep { it.id == siteName }.first()
    envNames =
        site.environments.collect { it.name } +
        site.environments.grep { it.perform }
            .collect { "${siteName}_${it.name}" }
    displayName('MyGov Site Fail Recover')
    parameters {
        choiceParam('env', envNames, trim('''\
            The holding page will be removed on your chosen environment
            (mygov_blu or mygov_grn redirects live site to blue or green ENV)'''))
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(trim('''\
            cd tools/management/
            ./mygov_sitefail_recover ${env}
            '''))
    }
}

view << job('gov-site-fail-recover') {
    siteName = "gov"
    site = sites.grep { it.id == siteName }.first()
    envNames =
        site.environments.collect { it.name } +
        site.environments.grep { it.perform }
            .collect { "${siteName}_${it.name}" }
    displayName('Gov Site Fail Recover')
    parameters {
        choiceParam('env', envNames, trim('''\
            The holding page will be removed on your chosen environment
            (gov_bgv or gov_ggv redirects live site to gov blue or gov green ENV)'''))
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(trim('''\
            cd tools/management/
            ./gov_sitefail_recover ${env}
            '''))
    }
}

sites.collect { site ->
    view << job("${site.id}-prerelease") {
        displayName("Make ${site.name} environment read-only")
        description(trim("""\
          Prevent changes on production environment and disable notifications

          This job stops the CMS and disables the authentication service to
          prevent changes being made on an environment. This ensures that
          changes are not lost during a blue/green release.
        """))
        def productionEnvironments = site.environments
            .grep { it.perform }
            .collect { it.name }
        parameters {
            choiceParam('env', productionEnvironments, 'environment')
        }
        scm {
            awsRepo(delegate)
        }
        steps {
            shell('tools/pre-release ${env}')
        }
        publishers {
            buildDescription('', '$env')
        }
    }

    view << job("${site.id}-rollback") {
        displayName("Make ${site.name} environment read-write")
        description(trim("""\
          Allow changes on production environment and enable monitoring notifications.

          This job can be used if a release needs to be cancelled between running the
          "Make ${site.name} environment read-only" and the blue/green switch job.

          This job can also be used if a release needs to be rolled back after the
          blue/green switch job has run but before the teardown of the old production
          environment. To rollback a release, run this job on the offline environment
          (the environment to rollback to), and run the blue/green switch, selecting
          the same environment. Note that rolling back a release will result in a loss
          of any changes or new data since the release.
        """))
        def productionEnvironments = site.environments
            .grep { it.perform }
            .collect { it.name }
        parameters {
            choiceParam('env', productionEnvironments, 'environment')
        }
        scm {
            awsRepo(delegate)
        }
        steps {
            shell('tools/rollback ${env}')
        }
        publishers {
            buildDescription('', '$env')
        }
    }

}

view << job('sync-repo') {
    displayName('Update S3 repository')
    steps {
        shell('pipeline sync')
    }
}

listView('Release') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        view.each {
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
