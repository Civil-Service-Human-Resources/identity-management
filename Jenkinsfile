pipeline {
    agent none
    stages {
        stage('Build & Push to ACR') {
            agent { label 'master' }
            steps {
                sh 'mvn package'
                script {
                    docker.withRegistry("${env.DOCKER_REGISTRY_URL}", 'docker_registry_credentials') {
                        def customImage = docker.build('identity-management')
                        customImage.push("${env.BRANCH_NAME}-${env.BUILD_ID}")
                    }
                }
            }
        }
        stage('Post') {
            agent { label 'master' }
            post {
                cleanup {
                    deleteDir()
                }
            }
        }
    }
}