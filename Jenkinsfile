pipeline {
    agent any

    environment {
        AWS_ACCOUNT_ID = "796008141374"
        AWS_REGION = "eu-north-1"
        IMAGE_REPO_NAME = "amazon-ecr-001"
    }

    stages {
        stage('Detect Branch') {
            steps {
                script {
                    BRANCH_NAME = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    IMAGE_TAG = BRANCH_NAME
                    REPOSITORY_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${IMAGE_REPO_NAME}"
                    echo "🔍 Detected branch: ${BRANCH_NAME}"
                }
            }
        }

        stage('Branch Check') {
            steps {
                script {
                    if (!(BRANCH_NAME == 'master' || BRANCH_NAME == 'develop' || BRANCH_NAME.startsWith('release') || BRANCH_NAME.startsWith('feature'))) {
                        echo "🚫 Skipping unsupported branch: '${BRANCH_NAME}'"
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                }
            }
        }

        stage('Build & Push') {
            when {
                expression {
                    return BRANCH_NAME == 'master' || BRANCH_NAME == 'develop' || BRANCH_NAME.startsWith('release') || BRANCH_NAME.startsWith('feature')
                }
            }
            stages {
                stage('Build JAR') {
                    steps {
                        echo "🔧 Building JAR..."
                        sh 'mvn clean package -DskipTests'
                    }
                }

                stage('Build Docker Image') {
                    steps {
                        echo "🐳 Building Docker image..."
                        sh "docker build -t ${IMAGE_REPO_NAME}:${IMAGE_TAG} ."
                    }
                }

                stage('Tag & Push to ECR') {
                    steps {
                        echo "🚀 Tagging and pushing image to ECR..."
                        withCredentials([usernamePassword(
                            credentialsId: 'aws-creds',
                            usernameVariable: 'AWS_ACCESS_KEY_ID',
                            passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                        )]) {
                            sh """
                                export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
                                export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY

                                aws ecr get-login-password --region ${AWS_REGION} | \
                                docker login --username AWS --password-stdin ${REPOSITORY_URI}

                                docker tag ${IMAGE_REPO_NAME}:${IMAGE_TAG} ${REPOSITORY_URI}:${IMAGE_TAG}
                                docker push ${REPOSITORY_URI}:${IMAGE_TAG}
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Image pushed for branch: ${BRANCH_NAME}"
        }
        failure {
            echo "❌ Pipeline failed for branch: ${BRANCH_NAME}"
        }
    }
}
