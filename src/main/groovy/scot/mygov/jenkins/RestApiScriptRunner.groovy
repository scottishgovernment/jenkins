package scot.mygov.jenkins

import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.ScriptRequest

String pattern = System.getProperty('pattern') ?: '*.groovy'
String baseUrl = System.getProperty('baseUrl')
String username = System.getProperty('username')
String password = System.getProperty('password') // password or token

if (!pattern || !baseUrl) {
    println 'usage: -DbaseUrl=<baseUrl> [-Dpattern=<pattern>] [-Dusername=<username>] [-Dpassword=<password>]'
    System.exit 1
}

RestApiJobManagement jm = new RestApiJobManagement(baseUrl)
if (username && password) {
    jm.setCredentials username, password
}

new FileNameFinder().getFileNames('jobs', pattern).each { String fileName ->
    println "\nprocessing file: $fileName"
    File file = new File(fileName)
    def url = new File(".").toURI().toURL()
    def req = new ScriptRequest(null, file.text, url)
    DslScriptLoader.runDslEngine(req, jm)
}
