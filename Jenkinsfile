pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
        disableConcurrentBuilds()
        timestamps()
    }
    
    agent any
    
    triggers {
        pollSCM('*/10 * * * *')
    }
        
    tools {
        maven 'maven_3.5.3'
    }
    
    parameters {
        booleanParam(defaultValue: false, description: 'Shell I deploy somewhere?', name: 'doDeploy')
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn -DforkCount=1 --settings maven_settings.xml clean install'
            }
        }
    }
    
    post {
        success {
            archiveArtifacts(
                artifacts: '**/*.jar',
                fingerprint: true
            )
            deleteDir()
        }
    }
}