import scot.mygov.jenkins.*

new JavaProject(name: 'beta-config').build(this)
new JavaProject(name: 'validation').build(this)

job("versions") {
  steps {
    shell("cd ../../mygov-seed/workspace")
    shell(readFileFromWorkspace('resources/set-build-id'))
  }
}
