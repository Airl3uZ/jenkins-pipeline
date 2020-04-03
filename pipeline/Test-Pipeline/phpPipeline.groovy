@Library('SharedLibrary')_
pipeline {
    agent any
    stages {
        stage("Checkout APP") {
            steps {
                dir("app") {
                    checkout([
                    $class: 'GitSCM',
                    branches: [[name: 'DEVELOP']],
                    doGenerateSubmoduleConfigurations: false,
                    userRemoteConfigs: [
                        [ url: 'git@dev-www.ibaht.com:dev_cfg.git' ], [credentialsId: 't2p-git']]
                    ])
                }
                dir('app/api_checkout') {
                    checkoutCode(
                        branch: "origin/DEVELOP",
                        appenv: "dev",
                        repo: "dev_api_t2pcheckoutv3",
                        credentialsId: "t2p-git"
                    )
                }
                dir('app/_inc')  {
                    checkoutCode(
                        branch: "origin/DEVELOP",
                        appenv: "dev",
                        repo: "dev_inc_main",
                        credentialsId: "t2p-git"
                    )                 
                }
            }
        }
        stage('UnitTest') {
            agent {
                docker {
                    args "-v app:/data"
                    image '986003803012.dkr.ecr.ap-southeast-1.amazonaws.com/nginxphp72:latest'
                    reuseNode true
                }
            }
            options {
                timeout(time: 5, unit: "MINUTES")
            }
            steps {
                dir('/data/api_checkout') {
                    echo "Composer Update"
                    sh 'composer update'
                    sh 'ls'
                    echo "Unit Test"
                    sh './vendor/bin/phpunit'
                }
            }
        }
        stage('SCA and Quality') {
            environment {
                scannerHome = tool 'sonar-scanner'
            }
            steps {   
                sonarprop = pwd()         
                sonarqubeScan(
                    file: "sonar.properties",
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