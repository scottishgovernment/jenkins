import scot.mygov.jenkins.*

new JavaProject(
  name: 'Authentication',
  repo: 'basic-authentication'
).build(this)

new JavaProject(
  name: 'Beta Config',
  repo: 'beta-config'
).build(this)

new JavaProject(
  name: 'Business Rates',
  repo: 'business-rates-service'
).build(this)

new JavaProject(
  name: 'Decommission Tool',
  repo: 'decommission-tool'
).build(this)

new JavaProject(
  name: 'Doctor',
  repo: 'doctor'
).build(this)

new JavaProject(
  name: 'Feedback',
  repo: 'feedback'
).build(this)

new JavaProject(
  name: 'Funding Tool',
  repo: 'funding-tool'
).build(this)

new JavaProject(
  name: 'GeoSearch',
  repo: 'geo-search'
).build(this)

new JavaProject(
  name: 'Health Check',
  repo: 'health-check'
).build(this)

new JavaProject(
  name: 'Pictor',
  repo: 'pictor'
).build(this)

new JavaProject(
  name: 'Press Releases',
  repo: 'press-releases'
).build(this)

new JavaProject(
  name: 'Publishing',
  repo: 'beta-publishing'
).build(this)

new JavaProject(
  name: 'Search',
  repo: 'beta-web-site-search'
).build(this)

new JavaProject(
  name: 'Utils',
  repo: 'mygovscot_utils'
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
