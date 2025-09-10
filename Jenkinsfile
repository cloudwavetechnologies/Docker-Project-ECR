pipeline {
    agent any

    environment {
        AWS_ACCOUNT_ID = "796008141374"
        AWS_REGION = "eu-north-1"
        IMAGE_REPO_NAME = "amazon-ecr-001"
        BRANCH = "${env.BRANCH_NAME ?: 'unknown'}"
        IMAGE_TAG = "${BRANCH}"
        REPOSITORY_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${IMAGE_REPO_NAME}"
    }

    stages {
        stage('Branch Check') {
            steps {
                script {
                    def branch = env.BRANCH_NAME ?: ""
                    if (!(branch == 'master' || branch == 'develop' || branch.startsWith('release') || branch.startsWith('feature'))) {
                        echo "🚫 Skipping unsupported branch: '${branch}'"
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                }
            }
        }

        stage('Build & Push') {
            when {
                expression {
                    def branch = env.BRANCH_NAME ?: ""
                    return branch == 'master' || branch == 'develop' || branch.startsWith('release') || branch.startsWith('feature')
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
            echo "✅ Image pushed for branch: ${env.BRANCH_NAME ?: 'unknown'}"
        }
        failure {
            echo "❌ Pipeline failed for branch: ${env.BRANCH_NAME ?: 'unknown'}"
        }
    }
}
