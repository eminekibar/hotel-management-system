pipeline {
    agent any

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
                    echo 'Docker İmajı Hazırlanıyor...'
                    sh "docker build -t ${DOCKER_IMAGE}:latest ."
                    
                    echo 'Docker Hub\'a Yükleniyor...'
                    withCredentials([usernamePassword(credentialsId: DOCKER_CREDS, usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                        sh "docker login -u $USER -p $PASS"
                        sh "docker push ${DOCKER_IMAGE}:latest"
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