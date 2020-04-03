@Library('SharedLibrary')_
pipeline {
    agent any
    stages {
        stage("check Project Properties") {
            steps {
                script  {
                    def props = readProperties  file: 'project.properties'
                    echo "${props.PROJECTNAME}"
                }
            }
        }
        stage("Checkout APP") {
            steps {
                new File("app").mkdir()
                dir("app") {
                    checkoutCode(
                        branch: "BUILD_T2PCHECKOUTAPI_DEV",
                        appenv: "dev",
                        repo: "build_configs"
                    )
                }
                new File("api_checkout").mkdir()
                dir('app/api_checkout') {
                    checkoutCode(
                        branch: "DEVELOP",
                        appenv: "dev",
                        repo: "dev_api_t2pcheckoutv3"
                    )
                }
                new File("_inc").mkdir()
                dir('app/_inc')  {
                    checkoutCode(
                        branch: "_INC_MAIN",
                        appenv: "dev",
                        repo: "dev_inc_main"
                    )                 
                }
            }
        }
        stage('UnitTest') {
            agent {
                docker {
                    args "-v ./:/data/api_checkout"
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