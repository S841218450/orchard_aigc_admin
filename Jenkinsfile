pipeline {
    agent any
    
    environment {
        // Spring Boot配置
        SPRING_IMAGE = 'orchard2026'
        SPRING_TAG = "${env.BUILD_NUMBER}"
        DOCKER_REGISTRY = 'registry.cn-shenzhen.aliyuncs.com/mynamespace'
        // 部署配置（删掉末尾多余斜杠）
        DEPLOY_HOST = '134.175.217.240'
        DEPLOY_USER = 'root'
        // 支持页面参数化构建，默认打包 orchard-app
        TARGET_MODULE = ''
    }

    // 开启参数化构建，页面可选择打包模块
//     parameters {
//         string(name: 'TARGET_MODULE', defaultValue: 'orchard-app', description: '指定构建微服务模块')
//     }
    
    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds() // 禁止同时构建，避免镜像冲突
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
                    # 传入动态模块参数构建
                    docker build -t ${SPRING_IMAGE}:${SPRING_TAG} \
                        --build-arg MODULE=${TARGET_MODULE} .
                    docker tag ${SPRING_IMAGE}:${SPRING_TAG} ${SPRING_IMAGE}:latest
                """
            }
        }
        
        stage('推送镜像到阿里云仓库 Push Images') {
            when { branch 'main' } // 仅main分支推送镜像
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-registry-credentials', 
                    usernameVariable: 'DOCKER_USER', 
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        docker login ${DOCKER_REGISTRY} -u ${DOCKER_USER} -p ${DOCKER_PASS}
                        # 打远程仓库标签
                        docker tag ${SPRING_IMAGE}:${SPRING_TAG} ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                        docker tag ${SPRING_IMAGE}:latest ${DOCKER_REGISTRY}/${SPRING_IMAGE}:latest
                        # 推送镜像
                        docker push ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                        docker push ${DOCKER_REGISTRY}/${SPRING_IMAGE}:latest
                        # 清理本地镜像释放磁盘
                        docker rmi ${SPRING_IMAGE}:${SPRING_TAG} ${SPRING_IMAGE}:latest || true
                    """
                }
            }
        }
        
        stage('远程部署 Deploy') {
            when { branch 'main' } // 仅main分支执行部署
            steps {
                sshagent(credentials: ['ssh-deploy-credentials']) {
                    sh """
                        # 外层双引号，变量正常解析
                        ssh ${DEPLOY_USER}@${DEPLOY_HOST} "
                            docker stop orchard2026 || true
                            docker rm orchard2026 || true
                            # 拉取阿里云最新镜像
                            docker pull ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                            # 启动容器（使用.env文件加载环境变量）
                            docker run -d --name orchard2026 \
                                -p 48080:48080 \
                                -v /home/www/orchard_aigc_admin/logs:/app/logs \
                                -v /home/www/orchard_aigc_admin/.env:/app/.env:ro \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                --env-file /home/www/orchard_aigc_admin/.env \
                                --restart unless-stopped \
                                ${DOCKER_REGISTRY}/${SPRING_IMAGE}:${SPRING_TAG}
                            # 清理远端旧镜像（可选）
                            docker image prune -f
                        "
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo "✅ 流水线执行成功！镜像版本：${SPRING_TAG}，模块：${TARGET_MODULE}"
        }
        failure {
            echo "❌ 流水线执行失败，请查看构建日志排查问题"
        }
        always {
            cleanWs() // 清空工作区代码缓存
        }
    }
}