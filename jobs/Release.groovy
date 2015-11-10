import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.trim

def view = []

view << job('blue-green-switch') {
    displayName('Blue-Green Switch')
    parameters {
        choiceParam('env', ['blu', 'grn'], 'mygov.scot production environment')
    }
    scm {
        git(repo('aws'))
    }
    steps {
        shell(trim('''\
            cd tools/management/
            ./aws_blugrn_switch.sh ${env}
        '''))
    }
    publishers {
        buildDescription('', '$env')
    }
}

view << job('site-fail-trigger') {
    displayName('MyGov Site Fail Trigger')
    scm {
        git(repo('aws'))
    }
    steps {
        shell(trim('''\
            cd tools/management/
            ./aws_sitefail_trigger.sh \
        '''))
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
