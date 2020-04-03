@Library('SharedLibrary')_
properties = null

def loadProperties() {
    node {
        properties = readProperties file: 'project.properties'
        println properties
        echo "Immediate one ${properties.repo}"
    }
}
pipeline {
    agent any
    stages {
        stage("Code Checkout") {
            steps {
                loadProperties()
                checkoutCode(
                    branch: "master",
                    appenv: "${properties.APP_ENV}",
                    repo: "${properties.REPO}"
                )                    
            }
        }
        stage('UnitTest') {
            agent {
                docker {
                    args "-v app:/app -p 9000:9000"
                    image 'webdevops/php:latest'
                    customWorkspace "php"
                    reuseNode true
                }
            }
            options {
                timeout(time: 5, unit: "MINUTES")
            }
            steps {
                dir('app') {
                    echo "Composer Update"
                    sh 'composer update'
                    sh 'ls'
                    sh './vendor/bin/phpunit'
                }
            }
        }
        stage('SCA and Quality') {
            environment {
                scannerHome = tool 'sonar-scanner'
            }
            steps {            
                sonarqubeScan(
                    file: "project.properties",
                    home: "${scannerHome}"
                )
            }
        }
        // stage("OWASP dependency check") {
        //     steps {
        //         sh "mkdir -p report/owasp_dependency_check"
        //         dependencyCheck additionalArguments: '--project testCI --scan app/** --out report/owasp_dependency_check/result.html --format HTML', odcInstallation: 'owasp-depend-chk'
        //     }
        // }
        stage("report") {
            steps {
                step([$class: 'CopyArtifact',
                projectName: 'CI_report',
                filter: 'result/*'])
                reportHTML(
                    reportDir: 'result/',
                    reportFiles: "testCI.html",
                    reportName: testCI_report
                )
            }
        }
    }
    post {
        always {
            cleanWs deleteDirs: true
        }
    }
}