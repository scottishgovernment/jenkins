import scot.mygov.jenkins.*

jobs = [
    [
        name: 'Authentication',
        repo: 'basic-authentication',
        snapshot: 'authentication-client,authentication-spring'
    ],
    [
        name: 'Beta Config',
        repo: 'beta-config',
        snapshot: '.'
    ],
    [
        name: 'Business Rates',
        repo: 'business-rates-service'
    ],
    [
        name: 'Decommission Tool',
        repo: 'decommission-tool'
    ],
    [
        name: 'Doctor',
        repo: 'doctor'
    ],
    [
        name: 'Feedback',
        repo: 'feedback'
    ],
    [
        name: 'Funding Tool',
        repo: 'funding-tool'
    ],
    [
        name: 'GeoSearch',
        repo: 'geo-search'
    ],
    [
        name: 'Health Check',
        repo: 'health-check'
    ],
    [
        name: 'Pictor',
        repo: 'pictor'
    ],
    [
        name: 'Press Releases',
        repo: 'press-releases'
    ],
    [
        name: 'Publishing',
        repo: 'beta-publishing',
        snapshot: 'publishing-api'
    ],
    [
        name: 'Search',
        repo: 'beta-web-site-search'
    ],
    [
        name: 'Utils',
        repo: 'mygovscot_utils',
        snapshot: '.'
    ],
    [
        name: 'Validation',
        repo: 'unified_validation',
        snapshot: '.'
    ]
]

jobs.each {
    new JavaProject(it).build(this)
}

job("versions") {
  steps {
    shell("cd ../../mygov-seed/workspace")
    shell(readFileFromWorkspace('resources/set-build-id'))
  }
}
