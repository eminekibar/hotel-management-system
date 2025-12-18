pipeline {
    agent any

    triggers {
        pollSCM('H/1 * * * *') 
    }

    tools {
        maven "M3" 
    }
    
    environment {
        DOCKER_IMAGE = 'eminekibar/hotel-management-system'
        DOCKER_CREDS = 'docker-hub-credentials'
    }

    stages {
        stage('Build') {
            steps {
                
                git branch: 'main', url: 'https://github.com/eminekibar/hotel-management-system.git'
                
                sh "mvn -Dmaven.test.skip=true clean package"
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def scannerHome = tool 'scanner'
                    withSonarQubeEnv('sonar-server') {
                        sh """
                            ${scannerHome}/bin/sonar-scanner \
                            -Dsonar.projectKey=hotel-management-system \
                            -Dsonar.projectName="Hotel Management System" \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.java.binaries=target/classes
                        """
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Docker Build & Push') {
            steps {
                script {
                    echo 'Docker İmajı Hazırlanıyor ve Docker Hub\'a Yükleniyor'
                    docker.withRegistry('', 'docker-hub-credentials') {
                    docker.build("${DOCKER_IMAGE}:latest").push()
                    }
                }
            }
        }
    }
    
    post {
        always {
            sh "docker logout"
        }
    }
}