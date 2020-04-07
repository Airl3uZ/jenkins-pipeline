#!groovy
def call(stageResults) {
    def token = "0vLiHpCXMxx5zKVYf9VdmQTRI61J250Eq2ozN3NW06M"
    def jobName = env.JOB_NAME +' '+env.BRANCH_NAME
    def buildNo = env.BUILD_NUMBER

    def url = 'https://notify-api.line.me/api/notify'
    def message1 = "${jobName} Build #${buildNo} ${stageResults.first} \r\n"
    def message2 = "${jobName} Build #${buildNo} ${stageResults.last} \r\n"
    def message = message1 + message2
    // sh "curl ${url} -H 'Authorization: Bearer ${token}' -F 'message=${message}'"
    echo "curl ${url} -H 'Authorization: Bearer ${token}' -F 'message=${message}'"
}
pipeline {
    agent any
    stages {
        stage('define variable') {
            steps {
                script {
                    def stageResults = ['first':'success', 'last':'success']
                    sh println stageResults
                }
            }
        }
        stage('First') {
            steps {
                stageResults.first = 'success!!'
            }
        }
        stage('Last') {
            steps {
                stageResults.last = 'success!!'
            }
        }
    }
    post {
        always {
            // notifyLINE(${stageResults})
            echo "${stageResult}"
        }
    }
}