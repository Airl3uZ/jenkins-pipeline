#!groovy
@Library('SharedLibrary')_
pipeline {
    agent any
    stages {
        stage("Checkout APP") {
            steps {
                echo "step one"
                // checkout([$class: 'GitSCM', branches: [[name: 'origin/DEVELOP']], doGenerateSubmoduleConfigurations: false,extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'data']],userRemoteConfigs: [[credentialsId: '75aa10b1-d3c0-4675-818f-73b572b08684', url: 'git@dev-www.ibaht.com:dev_cfg.git']]])
            }
        }
        // stage('checkout api_checkout') {
        //     steps {
        //         checkout([$class: 'GitSCM', branches: [[name: 'origin/DEVELOP']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'data/api_checkout']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '75aa10b1-d3c0-4675-818f-73b572b08684', url: 'git@dev-www.ibaht.com:dev_api_t2pcheckoutv3.git']]])
        //     }
        // }
        // stage('Checkout INC') {
        //     steps {
        //         checkout([$class: 'GitSCM', branches: [[name: 'origin/DEVELOP']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'data/_inc']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '75aa10b1-d3c0-4675-818f-73b572b08684', url: 'git@dev-www.ibaht.com:dev_inc.git']]]) 
        //     }
        // }
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
                        }
                        sh "pwd && ls -altr"
                        // echo "Composer Update"
                        // sh 'composer update'
                        // sh 'ls'
                        // echo "Unit Test"
                        // sh './vendor/bin/phpunit'
                    }
                }
                stage('SonarQube code analysis and Quality Gate') {
                    // environment {
                    //     scannerHome = tool name: 'sonar-scanner'
                    // }
                    steps {
                        // sh "printenv"
                        echo "Do Static code analysis with SonarQube"
                        // withSonarQubeEnv('T2P-SonarQube') { 
                        //     sh "${scannerHome}/bin/sonar-scanner -Dproject.settings=${env.WORKSPACE}/scripts/pipeline/Test-Pipeline/sonar-project.properties"
                        // }
                        // timeout(time: 10, unit: 'MINUTES') {
                        //     waitForQualityGate abortPipeline: true
                        // }
                    }
                }
            }
        }
    }
}