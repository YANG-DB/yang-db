pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }
    
    agent any
    
    tools {
        maven 'maven_3.5.3'
    }
    
    parameters {
        booleanParam(defaultValue: false, description: 'Shell I deploy somewhere?', name: 'doDeploy')
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts(
                    artifacts: '**/*.jar',
                    fingerprint: true
                )
            }
        }
    }
}