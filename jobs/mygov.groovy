import org.yaml.snakeyaml.Yaml
import scot.mygov.jenkins.*

import static scot.mygov.jenkins.Utils.repo
import static scot.mygov.jenkins.Utils.slug

/**
 * Returns the workspace of this seed project.
 */
def loadYaml(file) {
    new Yaml().load(readFileFromWorkspace("resources/" + file))
}

def jobs = loadYaml("mygov.yaml").jobs
def sites = loadYaml("environments.yaml").sites
def list = []
list.addAll(jobs.collect {
    out.println("Processing job ${it.name}")
    def type = it.remove('type')
    MyGovProject project
    if (type == 'java') {
        project = new JavaProject(it)
    } else if (type == 'shell') {
        project = new ShellProject(it)
    } else if (type == 'node') {
        project = new NodeProject(it)
    }
    project.build(this, sites, out)
})

job("set-build-id") {
  displayName('Set Build Number')
  steps {
    shell(readFileFromWorkspace('resources/set-build-id'))
  }
}

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

buildMonitorView('Dashboard') {
    statusFilter(StatusFilter.ENABLED)
    delegate.jobs {
        list.each {
            name(it.name)
        }
        name('Pipeline Tools')
    }
    configure { view ->
        view / title('Builds')
    }
}
