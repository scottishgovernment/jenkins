# MyGov Jenkins Jobs

This project is used for generating Jenkins job definitions for mygov.scot projects.

## Usage

To generate Jenkins job configurations to config.xml files, run:

    ./gradlew run

To update Jenkins job configurations on a running server, use:

    ./gradlew rest -DbaseUrl=http://jenkins:8080/ -Dusername=… -Dpassword=…

The optional parameter `-Dpattern=…` may also be specified to specify the groovy files from which jobs should be generated.

## Resources

* [Job DSL examples](https://github.com/sheehan/job-dsl-gradle-example) - provides examples of job definitions
* [Job DSL API](https://jenkinsci.github.io/job-dsl-plugin/) - reference for creating jobs and views
* [Job DSL wiki](https://github.com/jenkinsci/job-dsl-plugin/wiki) - documentation for Job DSL plugin
* [Job DSL plugin](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin) - Job DSL plugin page on the Jenkins wiki
