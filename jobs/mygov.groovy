import scot.mygov.jenkins.*

new JavaProject(
  name: 'Beta Config',
  repo: 'beta-config'
).build(this)
new JavaProject(
  name: 'Validation',
  repo: 'unified_validation'
).build(this)

job("versions") {
  steps {
    shell("cd ../../mygov-seed/workspace")
    shell(readFileFromWorkspace('resources/set-build-id'))
  }
}
