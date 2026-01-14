pipeline {
    agent any
    
    tools {
        maven 'maven3.9'
    }
    
    environment {
        scannerHome = tool 'Sonar'
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
        stage('Code Analysis') {
            steps {
                withSonarQubeEnv('Sonar') {
                    sh 'mvn sonar:sonar'
                }
                echo 'Code analysis completed.'
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
            mail to: 'malek.bokri@iteam-univ.tn',
            subject: "Failed: Build ${env.BUILD_NUMBER} of ${env.JOB_NAME}",
            body: "Check details at ${env.BUILD_URL}"
        }
    }
}