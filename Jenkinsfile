pipeline {
    agent any
    
    tools {
        maven 'maven3.9'
    }
    
    environment {
        APP_NAME = 'mon-app-springboot'
        DOCKER_HUB_REPO = 'mbokri/voting-app'
        CREDENTIALS_ID = 'dockerhub-login-devops'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        
        stage('Build Image') {
            steps {
                script {
                    customImage = docker.build("${DOCKER_HUB_REPO}:${env.BUILD_ID}")
                }
                echo 'Docker image built successfully.'
            }
        }
        
        stage('push Image') {
            steps {
                script {
                    docker.withRegistry('', CREDENTIALS_ID) {
                        customImage.push()
                        customImage.push('latest')
                    }
                }
                echo 'Image pushed to Docker Hub successfully.'
            }
        }
    }
    
    post {
        success {
            echo 'Build SUCCESS!'
        }
        failure {
            echo 'Build FAILED!'
            
        }
    }
}