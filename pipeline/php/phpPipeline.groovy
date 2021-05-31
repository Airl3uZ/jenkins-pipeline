#!groovy
@Library('SharedLibrary')_
pipeline {
    agent any
    stages {
        stage("Checkout APP") {
            parallel {
                stage('Checkout cfg') {
                    steps {
                        gitCheckout(
                            branch: 'origin/DEVELOP',
                            repo: 'dev_cfg.git',
                            url: 'git@dev-www.ibaht.com',
                            path: 'data'
                        )
                    }
                }
                stage('checkout api_checkout') {
                    steps {
                        gitCheckout(
                            branch: 'origin/DEVELOP',
                            repo: 'dev_api_t2pcheckoutv3.git',
                            url: 'git@dev-www.ibaht.com',
                            path: 'data/api_checkout'
                        )
                    }
                }
                stage('Checkout INC') {
                    steps {
                        gitCheckout(
                            branch: 'origin/DEVELOP',
                            repo: 'dev_inc.git',
                            url: 'git@dev-www.ibaht.com',
                            path: 'data/_inc'
                        )
                    }
                }
            }
        }
        stage("Tests") {
            parallel {
                stage('UnitTest') {
                    agent {
                        docker {
                            args "-v data:/app:rw"
                            image 'webdevops/php'
                            reuseNode true
                        }
                    }
                    steps {
                        dir('data/api_checkout') {
                            sh "pwd && ls -altr" 
                            sh 'composer update'
                            sh 'ls'
                            echo "Unit Test"
                            sh './vendor/bin/phpunit'
                        }
                    }
                }
                stage('SonarQube code analysis and Quality Gate') {
                    environment {
                        scannerHome = tool name: 'sonar-scanner'
                    }
                    steps {
                        // sh "printenv"
                        echo "Do Static code analysis with SonarQube"
                        withSonarQubeEnv('T2P-SonarQube') { 
                            sh "${scannerHome}/bin/sonar-scanner -Dproject.settings=${env.WORKSPACE}/scripts/pipeline/php/sonar-project.properties"
                        }
                        timeout(time: 10, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true
                        }
                    }
                    post {
                        always {
                            publishCoverage adapters: [sonarGenericCoverageAdapter('results/coverage/coverage.xml')], sourceFileResolver: sourceFiles('STORE_ALL_BUILD')
                            cleanWs()
                        }
                    }
                }
            }
        }
    }
}