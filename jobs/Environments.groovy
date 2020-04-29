import org.yaml.snakeyaml.Yaml
import data.Checker
import data.Migration
import data.Restore
import data.Revert
import data.Dbrestore
import environments.Puppet
import environments.VPC
import pipeline.Perform
import pipeline.Prepare
import pipeline.Promotion

def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def mygovView = []
def govView = []
def pipelineView = []
def sites = yaml.sites

Binding binding = new Binding()
binding.setVariable("dsl", this)
binding.setVariable("out", out)

def vpc = new VPC()
def migration = new Migration()
def prepare = new Prepare()
def perform = new Perform()
def puppet = new Puppet()
def promotion = new Promotion()
def restore = new Restore()
def revert = new Revert()
def dbrestore = new Dbrestore()
def checker = new Checker()

[
    vpc,
    migration,
    prepare,
    perform,
    puppet,
    promotion,
    restore,
    revert,
    dbrestore,
    checker,
].each { c ->
  c.setBinding(binding)
}

sites.collect { site ->
    out.println("Processing site ${site.domain}")
    if (site.id == "mygov"){
        mygovView += vpc.build(site)
    }
    if (site.id == "gov"){
        govView += vpc.build(site)
    }

    prepare.build(site)
    perform.build(site)

    def envNames = site.environments.collect { it.name }
    pipelineView << puppet.build(site, envNames)
    pipelineView << promotion.build(site, envNames)
    pipelineView << restore.build(site, envNames)
    pipelineView << revert.build(site, envNames)
    pipelineView << dbrestore.build(site, envNames)
}

migrationView = migration.build(sites)

gov = sites.find { it.id == "gov" }
govEnvironments = gov.environments.collect { it.name }
pipelineView << checker.build(gov, govEnvironments)

pipelineView << job('sync-repo') {
    displayName('Update S3 repository')
    steps {
        shell('pipeline sync')
    }
}

listView('Mygov') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        mygovView.each {
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

listView('Gov') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        govView.each {
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

listView('Migration') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        migrationView.each {
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
