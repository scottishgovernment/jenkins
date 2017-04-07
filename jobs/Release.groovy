import static build.Utils.repo
import static build.Utils.trim
import static build.Utils.awsRepo

import org.yaml.snakeyaml.Yaml

def view = []

def trigger() {
    yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
    sites = yaml.sites
    site = sites.grep { it.id == siteName }.first()
    envNames = site.environments.collect { it.name }
}

def recover() {
    trigger()
    prod = site.environments.grep { it.perform }.collect { it.name }
}

view << job('blue-green-switch') {
    displayName('Blue-Green Switch')
    parameters {
        choiceParam('env', ['blu', 'grn'], 'mygov.scot production environment')
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(trim('''\
            tools/switch-nagios ${env}
            tools/mygov-switch ${env}
        '''))
    }
    publishers {
        buildDescription('', '$env')
    }
}

view << job('bgv-ggv-govscot-switch') {
    displayName('Blue-Green Switch Gov.scot')
    parameters {
        choiceParam('env', ['bgv', 'ggv'], 'gov.scot production environment')
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(trim('''\
            tools/switch-nagios ${env}
            tools/gov-switch ${env}
        '''))
    }
    publishers {
        buildDescription('', '$env')
    }
}

view << job('mygov-site-fail-trigger') {
    siteName = "mygov"
    trigger()
    envNames << "mygov"
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
    trigger()
    envNames << "gov"
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
    recover()
    prod.each { env ->
      envNames << "${siteName}_${env}"
    }
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
    recover()
    prod.each { env ->
      envNames << "${siteName}_${env}"
    }
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
        shell('./tools/management/authentication_ctl ${env} ${domain} ${action}')
        shell('./tools/management/event_handlers.sh ${env} ${eventhandlers_action}')
    }
}

view << job('eventhandler-ctl') {
    displayName('Enable/disable event handlers')
    parameters {
        choiceParam('env', ['blu', 'grn', 'dev', 'exp', 'int', 'per', 'tst', 'uat', 'bgv', 'ggv', 'dgv', 'egv', 'igv', 'pgv', 'tgv', 'ugv' ], 'environment')
        choiceParam('action', ['enable', 'disable'], 'action')
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell('./tools/management/event_handlers.sh ${env} ${action}')
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
