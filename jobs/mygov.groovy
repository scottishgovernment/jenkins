import org.yaml.snakeyaml.Yaml
import scot.mygov.jenkins.*

import static scot.mygov.jenkins.Utils.repo

/**
 * Returns the workspace of this seed project.
 */
def File workspace() {
    File file = new File(JavaProject.class.getResource('.').toURI())
    for (int i = 0; i < 6; i++) {
        file = file.getParentFile()
    }
    return file;
}

def yaml = new Yaml().load(readFileFromWorkspace("resources/mygov.yaml"))
def jobs = yaml.get("jobs")
jobs.collect {
    out.println("Processing job ${it['name']}")
    def type = it.remove('type')
    MyGovProject project
    if (type == 'java') {
        project = new JavaProject(it)
    } else if (type == 'shell') {
        project = new ShellProject(it)
    } else if (type == 'node') {
        project = new NodeProject(it)
    }
    project.build(this, out)
}

new File(workspace(), "jobs.txt").withWriter { out ->
    jobs.each { out.println(it['name'] + "," + repo(it['repo'])) }
}

job("Set Build Number") {
  steps {
    shell("cd ../../mygov-seed/workspace")
    shell(readFileFromWorkspace('resources/set-build-id'))
  }
}
