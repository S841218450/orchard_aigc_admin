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
            }
        }

        stage('Maven构建镜像 Spring Boot Build') {
            steps {
                sh """
                    docker build -t ${SPRING_IMAGE}:${SPRING_TAG} \
                        --build-arg MODULE=orchard-app .
                    docker tag ${SPRING_IMAGE}:${SPRING_TAG} ${SPRING_IMAGE}:latest
                """
            }
        }

        stage('推送镜像到阿里云仓库 Push Images') {
            when { branch 'main' }
            steps {
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
            when { branch 'main' }
            steps {
                sshagent(credentials: ['ssh-deploy-credentials']) {
                    sh """
                        ssh ${DEPLOY_USER}@${DEPLOY_HOST} "
                            docker stop orchard2026 || true
                            docker rm orchard2026 || true
                            docker pull ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                            docker run -d --name orchard2026 \
                                -p 48080:48080 \
                                -v /home/www/orchard_aigc_admin/logs:/app/logs \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                --env-file /home/www/orchard_aigc_admin/.env \
                                --restart unless-stopped \
                                ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                            docker image prune -f
                        "
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ 流水线执行成功！镜像版本：${SPRING_TAG}，固定构建模块：orchard-app"
        }
        failure {
            echo "❌ 流水线执行失败，请查看构建日志排查问题"
        }
        always {
            cleanWs()
        }
    }
}