@Library('checkoutCode') _
pipeline {
    agent any
    // libraries {
    //     lib('checkoutCode@master')
    // }
    // stages {
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
        // stage('SCA') {
        //     environment {
        //         sonar = tool name: 'sonar-scanner'
        //     }
        //     steps {
        //         withSonarQubeEnv('T2P-SonarQube') {
        //         sh "${sonar}/bin/sonar-scanner -Dproject.settings=app/sonar-project.properties"
        //         } // submitted SonarQube taskId is automatically attached to the pipeline context
        //     }
        // }
        // stage("Quality Gate") {
        //     steps {
        //         timeout(time: 1, unit: 'HOURS') {
        //             // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
        //             // true = set pipeline to UNSTABLE, false = don't
        //             waitForQualityGate abortPipeline: true
        //         }
        //     }
        // }
        // stage("OWASP dependency check") {
        //     steps {
        //         sh "mkdir -p report/owasp_dependency_check"
        //         dependencyCheck additionalArguments: '--project testCI --scan app/** --out report/owasp_dependency_check/result.html --format HTML', odcInstallation: 'owasp-depend-chk'
        //     }
        // }
    }
    post {
        always {
            cleanWs deleteDirs: true
        }
    }
}