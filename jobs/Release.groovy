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

view << job('pre-release') {
    displayName('Prevent changes on live environment')
    description('Makes an environment read-only by stopping authentication')
    parameters {
        choiceParam('env', ['blu', 'grn', 'bgv', 'ggv'], 'environment')
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell('tools/pre-release ${env}')
    }
}

view << job('authentication-ctl') {
    displayName('Start/stop authentication')
    parameters {
        choiceParam('env', ['blu', 'grn', 'bgv', 'ggv'], 'environment')
        choiceParam('domain', ['mygov.scot', 'gov.scot'], 'domain')
        choiceParam('action', ['start', 'stop'], 'action')
        choiceParam('eventhandlers_action', ['enable', 'disable'], 'eventhandlers_action')
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell('tools/management/authentication_ctl ${env} ${domain} ${action}')
        shell('tools/management/event_handlers.sh ${env} ${eventhandlers_action}')
    }
}

view << job('gov-index-backup') {
    displayName('Backup Hippo Index')
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(trim('''\
          tools/backup-index
        '''))
    }
}

listView('Release') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        view.each {
            name(it.name)
        }
        name('mygov-release-prepare')
        name('mygov-release-perform')
        name('gov-release-prepare')
        name('gov-release-perform')
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
