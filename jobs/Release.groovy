import static build.Utils.repo
import static build.Utils.trim
import static build.Utils.awsRepo

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
    displayName('MyGov Site Fail Trigger')
    scm {
        awsRepo(delegate)
    }
    steps {
        shell(trim('''\
            cd tools/management/
            ./aws_sitefail_trigger.sh \
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
