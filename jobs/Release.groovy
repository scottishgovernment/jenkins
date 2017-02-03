import static build.Utils.repo
import static build.Utils.trim
import static build.Utils.awsRepo

// Start of block of code to get a listing of the mygov Environments from the ennvironments.yaml
import org.yaml.snakeyaml.Yaml
import data.Restore
import data.Revert
import environments.Puppet
import environments.VPC
import pipeline.Perform
import pipeline.Prepare
import pipeline.Promotion


def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def environmentsView = []
def pipelineView = []
def sites = yaml.sites
def envNames = []

Binding binding = new Binding()
binding.setVariable("dsl", this)
binding.setVariable("out", out)

def vpc = new VPC(binding)
def prepare = new Prepare(binding)
def perform = new Perform(binding)
def puppet = new Puppet(binding)
def promotion = new Promotion(binding)
def restore = new Restore(binding)
def revert = new Revert(binding)

sites.collect { site ->
  if (site.id == "mygov") {
    out.println("Processing site ${site.domain}")

    environmentsView += vpc.build(site)

    prepare.build(site)
    perform.build(site)

    envNames = site.environments.collect { it.name }
    pipelineView << puppet.build(site, envNames)
    pipelineView << promotion.build(site, envNames)
    pipelineView << restore.build(site, envNames)
    pipelineView << revert.build(site, envNames)
  }
}
// End of Block for listing of the mygov Environments

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
