pipeline {
    agent {
        kubernetes {
            cloud 'kubernetes'
		}
    }

    environment {
        // Disables strict checking and prevents writing to known_hosts
        GIT_SSH_COMMAND = "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '5', numToKeepStr: '10'))
    }

    tools {
        maven 'maven-3.9.12'
        jdk 'jdk-21'
    }

    stages {
        stage('Checkcout') {
            steps {
                checkout scmGit(branches: [[name: 'main']], extensions: [], userRemoteConfigs: [[credentialsId: '6b82eb9b-9547-4546-9d98-6d66891cd835', url: 'git@github.com:minininja/k8s-index.git'
                ]])
            }
        }
        stage('Build') {
            steps {
                echo "🚀 Running Build stage..."
                sh "mvn -DskipTests clean package"
            }
        }
        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: '03aa141b-3b8a-41ad-8043-39cc348fbf43', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh 'mvn package -DskipTests -Dnative -Dquarkus.container-image.build=true -Dquarkus.container-image.builder=jib -Dquarkus.container-image.push=true -Dquarkus.container-image.group=$DOCKER_USERNAME -Dquarkus.container-image.name=director -Dquarkus.container-image.username=$DOCKER_USERNAME -Dquarkus.container-image.password=$DOCKER_PASSWORD'
                }
            }
        }
//         stage('Apply Kubernetes files') {
//            steps {
//                withKubeConfig([credentialsId: 'user1', serverUrl: 'https://api.k8s.my-company.com']) {
//                     sh 'kubectl apply -n director -f src/main/k8s/sa.yaml'
//                     sh 'kubectl apply -n director -f src/main/k8s/deployment.yaml'
//                     sh 'kubectl apply -n director -f src/main/k8s/service.yaml'
//                }
//            }
//         }
    }

    post {
//         always {
//             echo "🧹 Cleaning up workspace..."
//         }
        success {
            echo "✅ Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed. Please check the logs."
        }
    }
}