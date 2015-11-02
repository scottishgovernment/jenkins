import scot.mygov.jenkins.*

jobs = [
    [
        name: 'Authentication',
        repo: 'basic-authentication',
        snapshot: 'authentication-client,authentication-spring',
        host: 'pubapp01',
        envs: 'both'
    ],
    [
        name: 'Beta Config',
        repo: 'beta-config',
        snapshot: '.'
    ],
    [
        name: 'Business Rates',
        repo: 'business-rates-service',
        host: 'pubapp01',
        envs: 'mygov'
    ],
    [
        name: 'Decommission Tool',
        repo: 'decommission-tool',
        host: 'pubapp01',
        envs: 'both'
    ],
    [
        name: 'Doctor',
        repo: 'doctor',
        host: 'sitapp01',
        envs: 'gov'
    ],
    [
        name: 'Feedback',
        repo: 'feedback',
        host: 'fbkapp01',
        envs: 'both'
    ],
    [
        name: 'Funding Tool',
        repo: 'funding-tool',
        host: 'pubapp01',
        envs: 'mygov'
    ],
    [
        name: 'GeoSearch',
        repo: 'geo-search',
        host: 'sitapp01',
        envs: 'mygov'
    ],
    [
        name: 'Health Check',
        repo: 'health-check',
        host: 'pubapp01',
        envs: 'both'
    ],
    [
        name: 'Pictor',
        repo: 'pictor',
        host: 'sitapp01',
        envs: 'gov'
    ],
    [
        name: 'Press Releases',
        repo: 'press-releases',
        host: 'pubapp01',
        envs: 'gov'
    ],
    [
        name: 'Publishing',
        repo: 'beta-publishing',
        snapshot: 'publishing-api',
        host: 'pubapp01',
        envs: 'both'
    ],
    [
        name: 'Search',
        repo: 'beta-web-site-search',
        host: 'sitapp01',
        envs: 'both'
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
    new JavaProject(it).build(this, out)
}

job("versions") {
  steps {
    shell("cd ../../mygov-seed/workspace")
    shell(readFileFromWorkspace('resources/set-build-id'))
  }
}
