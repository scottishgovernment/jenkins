import org.yaml.snakeyaml.Yaml
import build.AMI
import data.Checker
import data.Migration
import data.SpecificMigration
import data.Restore
import data.Revert
import data.Dbrestore
import environments.VPC
import environments.Whitelist
import pipeline.Perform
import pipeline.Prepare
import pipeline.Promotion
import test.EndToEnd
import test.Perceptual

def yaml = new Yaml().load(readFileFromWorkspace("resources/environments.yaml"))
def sites = yaml.sites

Binding binding = new Binding()
binding.setVariable("dsl", this)
binding.setVariable("out", out)

def vpc = new VPC()
def ami = new AMI()
def whitelist = new Whitelist()
def migration = new Migration()
def specificmigration = new SpecificMigration()
def prepare = new Prepare()
def perform = new Perform()
def promotion = new Promotion()
def restore = new Restore()
def revert = new Revert()
def dbrestore = new Dbrestore()
def checker = new Checker()
def endtoend = new EndToEnd()
def perceptual = new Perceptual()

[
    vpc,
    ami,
    migration,
    specificmigration,
    prepare,
    perform,
    promotion,
    restore,
    revert,
    dbrestore,
    checker,
    whitelist,
    endtoend,
    perceptual,
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
    jobs << specificmigration.build(site)
    jobs << prepare.build(site)
    jobs << perform.build(site)
    jobs << revert.build(site, envNames)
    jobs << whitelist.build(site, envNames)
    jobs.addAll(vpc.build(site))

    if (site.id == 'mygov') {
        jobs << endtoend.build(site.id, 'rubric', envNames)
        jobs << endtoend.build(site.id, 'mygov', envNames)
        jobs << endtoend.build(site.id, 'tradingnation', envNames)
    } else if (site.id == 'gov') {
        jobs << endtoend.build(site.id, 'gov', envNames)
        jobs << checker.build(site, envNames)
    }
    jobs << perceptual.build(site.id, envNames)

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
