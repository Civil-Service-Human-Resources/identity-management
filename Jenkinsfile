pipeline {
    agent none
    stages {
        stage('Maven Build') {
            agent { label 'master' }
            steps {
                sh 'mvn clean'
                sh 'mvn package'
                stash 'workspace'
            }
        }
        stage('Build Container & Push to ACR') {
            agent { label 'master' }
            steps {
                unstash 'workspace'
                script {
                    docker.withRegistry("${env.DOCKER_REGISTRY_URL}", 'docker_registry_credentials') {
                        def customImage = docker.build('identity-management')
                        customImage.push("${env.BRANCH_NAME}-${env.BUILD_ID}")
                    }
                }
                stash 'workspace'
            }
        }
        stage('Post') {
            agent { label 'master' }
            steps {
                unstash 'workspace'
            }
            post {
                cleanup {
                    deleteDir()
                }
            }
        }
    }
}