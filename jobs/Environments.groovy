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

Binding binding = new Binding()
binding.setVariable("dsl", this)
binding.setVariable("out", out)

def vpc = new VPC()
def prepare = new Prepare()
def perform = new Perform()
def puppet = new Puppet()
def promotion = new Promotion()
def restore = new Restore()
def revert = new Revert()

[vpc, prepare, perform, puppet, promotion, restore, revert].each { c ->
  c.setBinding(binding)
}

sites.collect { site ->
    out.println("Processing site ${site.domain}")
    environmentsView += vpc.build(site)

    prepare.build(site)
    perform.build(site)

    def envNames = site.environments.collect { it.name }
    pipelineView << puppet.build(site, envNames)
    pipelineView << promotion.build(site, envNames)
    pipelineView << restore.build(site, envNames)
    pipelineView << revert.build(site, envNames)
}

pipelineView << job('sync-repo') {
    displayName('Update S3 repository')
    steps {
        shell('pipeline sync')
    }
}

listView('Environments') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        environmentsView.each {
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

listView('Pipeline') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        pipelineView.each {
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
