import static build.Utils.repo
import static build.Utils.trim
import static build.Utils.awsRepo

import org.yaml.snakeyaml.Yaml

def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def sites = yaml.sites
def view = []

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
            cd tools/management/
            ./aws_blugrn_switch.sh ${env}
            ./monitoring_switch.sh ${env}
            ./event_handlers.sh ${env} enable
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
            cd tools/management/
            ./aws_bgvggv_switch.sh ${env}
            ./monitoring_switch.sh ${env}
            ./event_handlers.sh ${env} enable
        '''))
    }
    publishers {
        buildDescription('', '$env')
    }
}

view << job('site-fail-trigger') {
    site = sites.grep { it.id == "mygov" }.first()
    envNames = site.environments.collect { it.name }
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
            ./aws_sitefail_trigger.sh ${env}
        '''))
    }
}

view << job('site-fail-recover') {
    siteName = "mygov"
    site = sites.grep { it.id == siteName }.first()
    envNames = site.environments.collect { it.name }
    prod = site.environments.grep { it.perform }.collect { it.name }
    prod.each { env ->
      envNames << "${siteName}_${env}"
    }

    displayName('MyGov Site Fail Recover')
    parameters {
        choiceParam('env', envNames, trim('''\
            The holding page will be removed on your chosen environment
            (mygov_blu mygov_grn redirect live site to blu or grn ENV)'''))
    }
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(trim('''\
            cd tools/management/
            ./aws_sitefail_recover.sh ${env}
            '''))
    }
}

view << job('mygov-publishing-ctl') {
    displayName('Start/stop publishing')
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
        shell('./tools/management/publishing_ctl ${env} ${domain} ${action}')
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
