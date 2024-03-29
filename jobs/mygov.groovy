import org.yaml.snakeyaml.Yaml
import build.Artifact
import build.JavaProject
import build.MyGovProject
import build.NodeProject
import build.ShellProject
import build.UpstreamProject
import pipeline.Tools

/**
 * Returns the workspace of this seed project.
 */
def loadYaml(file) {
    new Yaml().load(readFileFromWorkspace("resources/" + file))
}

def jobs = loadYaml("mygov.yaml").jobs
def sites = loadYaml("environments.yaml").sites
def dashboard = []
def list = []
list.addAll(jobs.collect {
    out.println("Processing job ${it.name}")
    def type = it.remove('type')
    def artifacts = it.remove('artifacts')
    MyGovProject project
    if (type == 'java') {
        project = new JavaProject(it)
    } else if (type == 'shell') {
        project = new ShellProject(it)
    } else if (type == 'node') {
        project = new NodeProject(it)
    } else if (type == 'upstream') {
        project = new UpstreamProject(it)
    }
    project.artifacts = [:]
    if (artifacts) {
        project.artifacts += artifacts?.collectEntries { k, v ->
            def artifact = new Artifact(v)
            artifact.debian = v.debian ?: k
            artifact.hosts = artifact.hosts ?: [project.host]
            [k, artifact]
        }
    }
    if (it.debian) {
        def hosts = it.host ? [it.host] : []
        def artifact = new Artifact([
            debian: it.debian,
            maven: it.maven,
            hosts: hosts
        ])
        project.artifacts += [(it.debian): artifact]
    }
    def job = project.build(this, sites, out)
    if (type != "upstream") {
        dashboard << job
    }
    job
})

job("set-build-id") {
    displayName('Set Build Number')
    steps {
        shell(readFileFromWorkspace('resources/set-build-id'))
    }
}

list << new Tools().build(this)

listView('Builds') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        list.each {
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
