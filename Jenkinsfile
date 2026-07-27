pipeline {
    agent any

    environment {
        SPRING_IMAGE = 'orchard2026'
        SPRING_TAG = "${env.BUILD_NUMBER}"
        IMAGE_TAR = "${SPRING_IMAGE}-${SPRING_TAG}.tar"
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {
        stage('拉取代码 Checkout') {
            steps {
                checkout scm
                sh """
                    echo "=== WebHook分支：${env.GIT_BRANCH}"
                """
            }
        }

        stage('构建Docker镜像') {
            steps {
                sh """
                    docker build -t ${SPRING_IMAGE}:${SPRING_TAG} \
                        --build-arg MODULE=orchard-service .
                    docker tag ${SPRING_IMAGE}:${SPRING_TAG} ${SPRING_IMAGE}:latest
                """
            }
        }

        stage('本地直接部署（同机器无需SSH）') {
            steps {
                script {
                    String realBranch = env.GIT_BRANCH.replace("origin/", "")
                    println("处理后分支：${realBranch}")

                    if (realBranch == 'main') {
                        println("✅ main分支，执行本地部署")
                        sh """
                            set -euo pipefail
                            echo "停止旧容器"
                            docker stop orchard2026 || true
                            docker rm orchard2026 || true

                            echo "启动新版本容器"
                            docker run -d --name orchard2026 \
                                -p 48080:48080 \
                                -v /home/www/orchard_aigc_admin/logs:/app/logs \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                --env-file /home/www/orchard_aigc_admin/.env \
                                --restart unless-stopped \
                                ${SPRING_IMAGE}:${SPRING_TAG}

                            docker image prune -f
                            echo "当前运行容器列表"
                            docker ps | grep orchard2026
                        """
                    } else {
                        println("❌ 非main分支，跳过部署")
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ 流水线执行成功！镜像版本：${SPRING_TAG}"
        }
        failure {
            echo "❌ 流水线执行失败，请查看日志"
        }
        always {
            cleanWs()
        }
    }
}