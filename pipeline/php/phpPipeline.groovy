@Library('SharedLibrary')_
pipeline {
    agent any
    // libraries {
    //     lib('checkoutCode@master')
    // }
    stages {
        stage("Code Checkout") {
            steps {
                checkoutCode(
                    branch: "master",
                    url: "https://github.com/Airl3uZ/demo-php-ci.git",
                )                    
            }
        }
        // stage('UnitTest') {
        //     agent {
        //         docker {
        //             args "-v app:/app -p 9000:9000"
        //             image 'webdevops/php:latest'
        //             customWorkspace "php"
        //             reuseNode true
        //         }
        //     }
        //     steps {
        //         dir('app') {
        //             echo "Composer Update"
        //             sh 'composer update'
        //             sh 'ls'
        //             sh './vendor/bin/phpunit'
        //         }
        //     }
        // }
        stage('SCA and Quality') {
            environment {
                scannerHome = tool 'SonarQubeScanner'
            }
            steps {            
                sonarqube(
                    file: 'project.properties',
                    home: 'scannerHome',
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
                genHTMLReport(
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