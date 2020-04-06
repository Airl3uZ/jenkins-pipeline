#!groovy
@Library('SharedLibrary')_
pipeline {
    agent any
    environment {
        report = "${env.resultPath}/php-demo"
    }
    stages {
        stage("Checkout APP") {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: 'origin/citest']],doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/Airl3uZ/demo-php-ci.git']]])
            }
        }
        stage("Tests") {
            parallel {
                stage('UnitTest') {
                    agent {
                        docker {
                            args "-v app:/app"
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
                publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'result', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: 'PHP-Test-Pipeline-Results'])
            }
        }
    }
    // post {
    //     always {
    //         cleanWs()
    //     }
    //     unstable {
    //         echo "UNSTABLE runs after ALWAYS"
    //     }
    // }
}