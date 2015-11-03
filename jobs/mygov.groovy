import scot.mygov.jenkins.*

jobs = [
    [
        name: 'Authentication',
        repo: 'basic-authentication',
        snapshot: 'authentication-client,authentication-spring',
        host: 'pubapp01',
        site: 'both'
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
        site: 'mygov'
    ],
    [
        name: 'Decommission Tool',
        repo: 'decommission-tool',
        host: 'pubapp01',
        site: 'both'
    ],
    [
        name: 'Doctor',
        repo: 'doctor',
        host: 'sitapp01',
        site: 'gov'
    ],
    [
        name: 'Feedback',
        repo: 'feedback',
        host: 'fbkapp01',
        site: 'both'
    ],
    [
        name: 'Funding Tool',
        repo: 'funding-tool',
        host: 'pubapp01',
        site: 'mygov'
    ],
    [
        name: 'GeoSearch',
        repo: 'geo-search',
        host: 'sitapp01',
        site: 'mygov'
    ],
    [
        name: 'Health Check',
        repo: 'health-check',
        host: 'pubapp01',
        site: 'both'
    ],
    [
        name: 'Pictor',
        repo: 'pictor',
        host: 'sitapp01',
        site: 'gov'
    ],
    [
        name: 'Press Releases',
        repo: 'press-releases',
        host: 'pubapp01',
        site: 'gov'
    ],
    [
        name: 'Publishing',
        repo: 'beta-publishing',
        snapshot: 'publishing-api',
        host: 'pubapp01',
        site: 'both'
    ],
    [
        name: 'Search',
        repo: 'beta-web-site-search',
        host: 'sitapp01',
        site: 'both'
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
