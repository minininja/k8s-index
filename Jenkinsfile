pipeline {
    agent {
        kubernetes {
            cloud 'kubernetes'
//             workspaceVolume dynamicPVC (
//                 accessModes: 'ReadWriteOnce',
//                 requestsSize: '1Gi'
//             )
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
        // Uncomment and configure tools as needed
        maven 'maven-3.9.12'
        jdk 'jdk-21'
    }

    stages {
        stage('Checkcout') {
            steps {
                checkout scmGit(branches: [[name: 'main']], extensions: [], userRemoteConfigs: [[credentialsId: '6b82eb9b-9547-4546-9d98-6d66891cd835', url: 'git@github.com:minininja/k8s-index.git']])
            }
        }
        stage('Build') {
            steps {
                echo "🚀 Running Build stage..."
                withMaven(
                    traceability: true
                ) {
                    // All steps inside this block will use the specified global settings
                    sh "mvn -DskipTests clean package"
                }
            }
        }
        stage('Test') {
            steps {
                echo "🚀 Running Test stage..."
                sh "mvn test"
                junit stdioRetention: 'ALL', testResults: '**/*.xml'
            }
        }
        stage('Push') {
            steps {
                withMaven(traceability: true) {
                    sh 'mvn package -DskipTests -Dquarkus.container-image.group=mikej091 -Dquarkus.jib.platforms=linux/amd64,linux/arm64/v8 -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true'
                }
            }
        }
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