pipeline {
    agent any

    environment {
        SPRING_IMAGE = 'orchard2026'
        SPRING_TAG = "${env.BUILD_NUMBER}"
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
                    echo "✅ 镜像构建完成 ${SPRING_IMAGE}:${SPRING_TAG}"
                """
            }
        }

        stage('本地直接部署') {
            steps {
                script {
                    String realBranch = env.GIT_BRANCH.replace("origin/", "")
                    println("处理后分支：${realBranch}")

                    if (realBranch == 'main') {
                        println("✅ main分支，执行本地部署")
                        sh """
                            echo "停止旧容器"
                            docker stop orchard2026 || true
                            docker rm orchard2026 || true

                            echo "启动容器"
                            CONTAINER_ID=\$(docker run -d --name orchard2026 \
                                -p 48080:48080 \
                                -v /home/www/orchard_aigc_admin/logs:/app/logs \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                --env-file /home/www/orchard_aigc_admin/.env \
                                --restart unless-stopped \
                                ${SPRING_IMAGE}:${SPRING_TAG})
                            echo "容器ID: \${CONTAINER_ID}"

                            sleep 4
                            echo "==== 所有容器 ===="
                            docker ps -a | grep orchard2026

                            if ! docker ps --filter "name=orchard2026" | grep orchard2026 ; then
                                echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                                echo "容器后台退出，打印应用日志"
                                echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                                docker logs orchard2026
                                exit 1
                            fi

                            echo "✅ 服务正常运行"

                            echo "==== 清理历史版本镜像（保留最近2个版本 + latest）===="
                            KEEP=2
                            docker images --format '{{.Repository}}:{{.Tag}}' \
                                | grep "^${SPRING_IMAGE}:" | grep -v ":latest" \
                                | sort -V -r | tail -n +\$((KEEP + 1)) \
                                | xargs -r docker rmi || true
                            echo "==== 当前 orchard2026 镜像 ===="
                            docker images ${SPRING_IMAGE}

                            docker image prune -f
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
            echo "❌ 流水线执行失败，请查看上方应用崩溃日志"
        }
        always {
            cleanWs()
        }
    }
}