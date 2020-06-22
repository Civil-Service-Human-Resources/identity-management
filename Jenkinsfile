pipeline {
    agent none

    environment {
        jwt_key = "test-key"
    }

    stages {
        stage('Build & Push to ACR') {
            agent { label 'master' }
            steps {
                sh 'mvn clean package'
                script {
                    docker.withRegistry("${env.DOCKER_REGISTRY_URL}", 'docker_registry_credentials') {
                        def customImage = docker.build("identity-management:${env.BRANCH_NAME}-${env.BUILD_ID}")
                        customImage.push("${env.BRANCH_NAME}-${env.BUILD_ID}")
                    }
                }
                deleteDir()
            }
        }
    }
}