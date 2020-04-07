@Library('SharedLibrary')_
pipeline {
    agent any
    stages {
        stage("Checkout APP") {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: 'origin/DEVELOP']], doGenerateSubmoduleConfigurations: false,extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'data']],userRemoteConfigs: [[credentialsId: '75aa10b1-d3c0-4675-818f-73b572b08684', url: 'git@dev-www.ibaht.com:dev_cfg.git']]])
            }
        }
        stage('checkout api_checkout') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: 'origin/DEVELOP']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'data/api_checkout']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '75aa10b1-d3c0-4675-818f-73b572b08684', url: 'git@dev-www.ibaht.com:dev_api_t2pcheckoutv3.git']]])
            }
        }
        stage('Checkout INC') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: 'origin/DEVELOP']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'data/_inc']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '75aa10b1-d3c0-4675-818f-73b572b08684', url: 'git@dev-www.ibaht.com:dev_inc.git']]]) 
            }
        }
        stage("Tests") {
            parallel {
                stage('UnitTest') {
                    agent {
                        docker {
                            args "-v data:/app"
                            image 'webdevops/php'
                            reuseNode true
                        }
                    }
                    options {
                        timeout(time: 10, unit: "MINUTES")
                    }
                    steps {
                        dir('app') {
                            echo "Composer Update"
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
                            // echo "${env.WORKSPACE}"
                            // sh "pwd && ls -altr"  
                            sh "${scannerHome}/bin/sonar-scanner -Dproject.settings=${env.WORKSPACE}/scripts/pipeline/Test-Pipeline/sonar-project.properties"
                        }
                        timeout(time: 10, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true
                        }
                    }
                }
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