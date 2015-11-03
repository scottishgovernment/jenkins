import org.yaml.snakeyaml.Yaml
import scot.mygov.jenkins.*

def yaml = new Yaml().load(readFileFromWorkspace("resources/mygov.yaml"))
yaml.get("jobs").each {
    new JavaProject(it).build(this, out)
}

job("versions") {
  steps {
    shell("cd ../../mygov-seed/workspace")
    shell(readFileFromWorkspace('resources/set-build-id'))
  }
}
