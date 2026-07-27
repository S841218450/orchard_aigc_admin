pipeline {
    agent any

    environment {
        SPRING_IMAGE = 'orchard2026'
        SPRING_TAG = "${env.BUILD_NUMBER}"
        DOCKER_REGISTRY = 'registry.cn-shenzhen.aliyuncs.com/mynamespace'
        DEPLOY_HOST = '134.175.217.240'
        DEPLOY_USER = 'root'
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
                    echo "=== git本地指针 ==="
                    git rev-parse --abbrev-ref HEAD
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
                """
            }
        }

        stage('推送镜像到阿里云仓库 Push Images') {
            // ❗删掉when，在脚本内部判断分支
            steps {
                sh """
                    echo "【推送阶段】本次推送分支: ${env.GIT_BRANCH}"
                    if [ "${env.GIT_BRANCH}" != "main" ]; then
                        echo "非main分支，跳过推送镜像"
                        exit 0
                    fi
                """
                withCredentials([usernamePassword(
                    credentialsId: 'docker-registry-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        docker login ${DOCKER_REGISTRY} -u ${DOCKER_USER} -p ${DOCKER_PASS}
                        docker tag ${SPRING_IMAGE}:${SPRING_TAG} ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                        docker tag ${SPRING_IMAGE}:latest ${DOCKER_REGISTRY}/${SPRING_IMAGE}:latest
                        docker push ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                        docker push ${DOCKER_REGISTRY}/${SPRING_IMAGE}:latest
                        docker rmi ${SPRING_IMAGE}:${SPRING_TAG} ${SPRING_IMAGE}:latest || true
                    """
                }
            }
        }

        stage('远程部署 Deploy') {
            // ❗删掉when，在脚本内部判断分支
            steps {
                sh """
                    echo "【部署阶段】本次推送分支: ${env.GIT_BRANCH}"
                    if [ "${env.GIT_BRANCH}" != "main" ]; then
                        echo "非main分支，跳过远程部署"
                        exit 0
                    fi
                """
                sshagent(credentials: ['ssh-deploy-credentials']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_HOST} '
                            set -euo pipefail
                            echo "=== 开始清理旧容器 ==="
                            docker stop orchard2026 || true
                            docker rm orchard2026 || true
                            echo "=== 拉取镜像 ==="
                            docker pull ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                            echo "=== 启动新容器 ==="
                            docker run -d --name orchard2026 \
                                -p 48080:48080 \
                                -v /home/www/orchard_aigc_admin/logs:/app/logs \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                --env-file /home/www/orchard_aigc_admin/.env \
                                --restart unless-stopped \
                                ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                            echo "=== 容器启动完成 ==="
                            docker ps | grep orchard2026
                            docker image prune -f
                        '
                    """
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
        }
        always {
            cleanWs()
        }
    }
}