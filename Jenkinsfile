pipeline {
    agent any

    environment {
        AWS_ACCOUNT_ID       = "093326771949"
        AWS_REGION           = "eu-north-1"
        IMAGE_REPO_NAME      = "amazon-ecr-001"
        LAMBDA_FUNCTION_NAME = "amazon-java-code-lambda-001"
        JAR_NAME             = "supplychain-project-1.0-SNAPSHOT.jar"
        S3_BUCKET            = "supplychain-s3-000"
        S3_KEY_PREFIX        = "Infra-folder"
    }

    tools {
        maven 'mvn'
    }

    stages {
        stage('Checkout & Branch Filter') {
            steps {
                script {
                    def branch = env.BRANCH_NAME ?: sh(
                        script: "git rev-parse --abbrev-ref HEAD",
                        returnStdout: true
                    ).trim()

                    echo "🔍 Checked out branch: ${branch}"

                    if (!(branch == 'master' ||
                          branch ==~ /develop.*/ ||
                          branch ==~ /release.*/ ||
                          branch ==~ /feature.*/)) {
                        echo "🚫 Skipping unsupported branch: '${branch}'"
                        currentBuild.result = 'SUCCESS'
                        return
                    }

                    env.BRANCH_NAME = branch
                }
            }
        }

        stage('Build JAR') {
            when {
                expression {
                    return env.BRANCH_NAME != null
                }
            }
            steps {
                echo "🔧 Building JAR..."
                sh 'mvn clean install'
            }
        }

        stage('Upload to S3') {
            when {
                expression {
                    return env.BRANCH_NAME != null
                }
            }
            steps {
                echo "📦 Uploading JAR to S3 bucket path: ${S3_KEY_PREFIX}/"
                sh "aws s3 cp target/${JAR_NAME} s3://${S3_BUCKET}/${S3_KEY_PREFIX}/"
                sh "aws s3 ls s3://${S3_BUCKET}/${S3_KEY_PREFIX}/"
            }
        }

        stage('Update Lambda Config') {
            when {
                expression {
                    return env.BRANCH_NAME != null
                }
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
            }
        }
    }
}
