import org.yaml.snakeyaml.Yaml
import build.AMI
import data.Checker
import data.Migration
import data.Restore
import data.Revert
import data.Dbrestore
import environments.Puppet
import environments.VPC
import environments.Whitelist
import pipeline.Perform
import pipeline.Prepare
import pipeline.Promotion

def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def sites = yaml.sites

Binding binding = new Binding()
binding.setVariable("dsl", this)
binding.setVariable("out", out)

def vpc = new VPC()
def ami = new AMI()
def whitelist = new Whitelist()
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
    ami,
    migration,
    prepare,
    perform,
    puppet,
    promotion,
    restore,
    revert,
    dbrestore,
    checker,
    whitelist,
].each { c ->
  c.setBinding(binding)
}

sites.collect { site ->
    out.println("Processing site ${site.domain}")

    def envNames = site.environments.collect { it.name }
    def jobs = []
    jobs << ami.build(site)
    jobs << promotion.build(site, envNames)
    jobs << dbrestore.build(site, envNames)
    jobs << restore.build(site, envNames)
    jobs << migration.build(site, envNames)
    jobs << prepare.build(site)
    jobs << perform.build(site)
    jobs << puppet.build(site, envNames)
    jobs << revert.build(site, envNames)
    jobs << whitelist.build(site, envNames)
    jobs.addAll(vpc.build(site))

    if (site.id == "gov") {
        jobs << checker.build(site, envNames)
    }

    listView(site.name) {
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
}
