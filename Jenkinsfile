pipeline {
    agent any

    environment {
        SPRING_IMAGE = 'orchard2026'
        SPRING_TAG = "${env.BUILD_NUMBER}"
        DEPLOY_HOST = '134.175.217.240'
        DEPLOY_USER = 'root'
        # 临时镜像包名称
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
                    echo "=== WebHook原始推送分支 GIT_BRANCH ==="
                    echo ${env.GIT_BRANCH}
                """
            }
        }

        stage('Maven构建镜像 Spring Boot Build') {
            steps {
                sh """
                    docker build -t ${SPRING_IMAGE}:${SPRING_TAG} \
                        --build-arg MODULE=orchard-service .
                    docker tag ${SPRING_IMAGE}:${SPRING_TAG} ${SPRING_IMAGE}:latest
                    # 将镜像打包为tar文件
                    docker save -o ${IMAGE_TAR} ${SPRING_IMAGE}:${SPRING_TAG}
                """
            }
        }

        stage('传输镜像至业务服务器 + 远程部署 Deploy') {
            steps {
                script {
                    String realBranch = env.GIT_BRANCH.replace("origin/", "")
                    println("原始分支：${env.GIT_BRANCH}，处理后分支：${realBranch}")

                    if (realBranch == 'main') {
                        println("✅ main分支，开始传输镜像并部署")
                        sshagent(credentials: ['ssh-deploy-credentials']) {
                            sh """
                                # 1. 将本地tar镜像scp传到服务器/root目录
                                scp -o StrictHostKeyChecking=no ${IMAGE_TAR} ${DEPLOY_USER}@${DEPLOY_HOST}:/root/

                                # 2. 远程服务器加载镜像、重启容器
                                ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_HOST} '
                                    set -euo pipefail
                                    echo "=== 加载镜像tar包 ==="
                                    docker load -i /root/${IMAGE_TAR}

                                    echo "=== 停止并删除旧容器 ==="
                                    docker stop orchard2026 || true
                                    docker rm orchard2026 || true

                                    echo "=== 启动新版本容器 ==="
                                    docker run -d --name orchard2026 \
                                        -p 48080:48080 \
                                        -v /home/www/orchard_aigc_admin/logs:/app/logs \
                                        -e SPRING_PROFILES_ACTIVE=prod \
                                        --env-file /home/www/orchard_aigc_admin/.env \
                                        --restart unless-stopped \
                                        ${SPRING_IMAGE}:${SPRING_TAG}

                                    echo "=== 清理服务器上的镜像tar包 ==="
                                    rm -f /root/${IMAGE_TAR}
                                    docker image prune -f

                                    echo "=== 当前运行容器 ==="
                                    docker ps | grep orchard2026
                                '

                                # Jenkins本机清理tar包，释放磁盘
                                rm -f ${IMAGE_TAR}
                            """
                        }
                    } else {
                        println("❌ 非main分支，跳过部署")
                        sh "rm -f ${IMAGE_TAR}"
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
            echo "❌ 流水线执行失败，请查看构建日志排查问题"
            sh "rm -f ${IMAGE_TAR}"
        }
        always {
            cleanWs()
        }
    }
}