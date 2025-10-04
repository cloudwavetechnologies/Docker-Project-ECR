pipeline {
    agent none

    environment {
        AWS_ACCOUNT_ID       = "093326771949"
        AWS_REGION           = "eu-north-1"
        IMAGE_REPO_NAME      = "amazon-ecr-001"
        LAMBDA_FUNCTION_NAME = "amazon-java-code-lambda-001"
        JAR_NAME             = "myapp-jar-with-dependencies.jar"
        S3_BUCKET            = "supplychain-s3-000"
        S3_KEY_PREFIX        = "Infra-folder"
    }

    tools {
        maven 'Maven3' // ✅ Updated to match your configured tool name
    }

    stages {
        stage('Checkout & Branch Filter') {
            agent { label 'master-agent' }
            steps {
                script {
                    def branch = 'feature/lambda-s3-trigger'
                    echo "🔍 Using hardcoded branch: '${branch}'"

                    if (branch == 'master' ||
                        branch ==~ /^develop.*/ ||
                        branch ==~ /^release.*/ ||
                        branch ==~ /^feature.*/) {
                        echo "✅ Supported branch detected: '${branch}'"
                        env.BRANCH_NAME = branch
                    } else {
                        echo "🚫 Unsupported branch: '${branch}' — skipping pipeline execution."
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                }
            }
        }

        stage('Parallel Build & Validation') {
            when {
                expression { env.BRANCH_NAME != null }
            }
            parallel {
                stage('Build on Master Agent') {
                    agent { label 'master-agent' }
                    tools { maven 'Maven3' }
                    steps {
                        echo "🔧 Building JAR on master-agent..."
                        sh 'mvn clean package'
                    }
                }

                stage('Validate on Slave2') {
                    agent { label 'slave2' }
                    steps {
                        echo "🔍 Validating environment on slave2..."
                        sh 'java -version'
                        sh 'df -h'
                        sh 'free -m'
                    }
                }
            }
        }

        stage('Upload to S3') {
            when {
                expression { env.BRANCH_NAME != null }
            }
            agent { label 'master-agent' }
            environment {
                AWS_ACCESS_KEY_ID     = credentials('aws-access-key-id')
                AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
            }
            steps {
                echo "📦 Uploading JAR to S3 bucket path: ${S3_KEY_PREFIX}/"

                sh """
                    if [ ! -f target/${JAR_NAME} ]; then
                        echo '❌ JAR file not found: target/${JAR_NAME}'
                        ls target/
                        exit 1
                    fi
                """

                sh "aws s3 cp target/${JAR_NAME} s3://${S3_BUCKET}/${S3_KEY_PREFIX}/"
                sh "aws s3 ls s3://${S3_BUCKET}/${S3_KEY_PREFIX}/"
            }
        }

       /* stage('Update Lambda Config') {
            when {
                expression { env.BRANCH_NAME != null }
            }
            agent { label 'slave2' }
            environment {
                AWS_ACCESS_KEY_ID     = credentials('aws-access-key-id')
                AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
            }
            steps {
                echo "🔄 Updating Lambda function code from S3..."

                sh """
                    aws lambda update-function-code \
                        --function-name ${LAMBDA_FUNCTION_NAME} \
                        --s3-bucket ${S3_BUCKET} \
                        --s3-key ${S3_KEY_PREFIX}/${JAR_NAME} \
                        --region ${AWS_REGION}
                """

                echo "⏳ Waiting for Lambda update to complete..."
                sleep(time: 20, unit: 'SECONDS')

                sh """
                    aws lambda update-function-configuration \
                        --function-name ${LAMBDA_FUNCTION_NAME} \
                        --handler com.cloudwavetechnologies.Main::handleRequest \
                        --region ${AWS_REGION}
                """
            }
        }
    }/*

    post {
        always {
            echo "🧹 Cleaning up workspace..."
            cleanWs()
        }
    }
}
