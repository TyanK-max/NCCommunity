# NCCommunity (仿牛客论坛项目——增量版v1.0)
## 本论坛在牛客论坛的基础上，新增了文件的上传和下载功能

> 关于部署项目，建议使用阿里云的按量付费实例，搞一个4核8G的服务器，这样运行起来比较丝滑。

### 使用docker部署该项目，避免繁杂的生产环境配置。

1. 防止预置了docker，先删除一波：

   ```bash
   sudo yum remove docker \
                     docker-client \
                     docker-client-latest \
                     docker-common \
                     docker-latest \
                     docker-latest-logrotate \
                     docker-logrotate \
                     docker-engine
   ```

2. 再下载：

   ```bash
   #安装软件包
   yum install -y yum-utils
   
   #安装docker阿里镜像源
   sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
   
   #安装最新版本的docker
   yum install docker-ce docker-ce-cli containerd.io
   
   #启动docker
   systemctl start docker
   
   #测试docker能否正常运行
   docker run hello-world
   
   #设置开机自启动
   systemctl enable docker
   ```

3. docker部署Elasticsearch

   ```bash
   #创建软件的文件夹 一般在/opt
   mkdir -p /opt/docker/es/plugins
   
   #下载镜像
   docker pull elasticsearch:7.17.7
   
   #放入容器运行
   docker run --name elasticsearch -p 9200:9200 -p 9300:9300  -v /opt/docker/es/plugins:/usr/share/elasticsearch/plugins -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms512m -Xmx512m -Duser.timezone=GMT+08" -d elasticsearch:7.17.7
   ```

   下载ik分词器插件，放在plugins文件夹内形成一个ik文件夹

   ```bash
   #更改分词器内配置文件 plugin-descriptor.properties 改为对应的ES版本
   elasticsearch.version=7.17.7
   ```

4. docker部署Zookeeper

   ```bash
   #下载镜像
   docker pull wurstmeister/zookeeper
   
   #启动容器
   docker run -d --name zookeeper -p 2181:2181 -v /etc/localtime:/etc/localtime wurstmeister/zookeeper
   ```

5. docker部署Kafka

   ```bash
   #下载镜像
   docker pull wurstmeister/kafka
   
   #启动容器  x.x.x.x为自己服务器的ipv4地址
   docker run -d --name kafka -p 9092:9092 \
   -e KAFKA_BROKER_ID=0 \
   -e KAFKA_ZOOKEEPER_CONNECT=x.x.x.x:2181 \
   -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://x.x.x.x:9092 \
   -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
   -v /etc/localtime:/etc/localtime \
   wurstmeister/kafka
   ```

6. docker部署MySQL

   ```bash
   #创建文件夹用于挂载卷
   mkdir /opt/docker/mysql/conf
   mkdir /opt/docker/mysql/data
   
   #直接运行容器 ****为自己设定MySQL密码
   docker run \
   --name mysql \
   -e MYSQL_ROOT_PASSWORD=**** \
   -p 3306:3306 \
   -v /opt/docker/mysql/conf/hmy.cnf:/etc/mysql/conf.d/hmy.cnf \
   -v /opt/docker/mysql/data:/var/lib/mysql \
   -d \
   mysql:8.0 --lower-case-table-names=1
   ```

   后面建议直接用自己的数据库连接工具（Navicat，SQLyog等）连接，执行sql文件。

7. docker 部署redis

   ```bash
   #下载镜像
   docker pull redis:7.0.11
   
   #创建挂载卷的文件夹
   mkdir -p /opt/docker/redis/conf
   mkdir -p /opt/docker/redis/data
   
   #增加配置文件
   vim /opt/docker/redis/conf/redis.conf
   
   #启动容器
   docker run -p 6379:6379 --name redis --privileged=true \
   -v /opt/docker/redis/conf:/etc/redis \
   -v /opt/docker/redis/data:/data \
   -d redis redis-server /etc/redis/redis.conf --appendonly yes
   ```

8. 使用dockerfile 制作项目镜像

   ```bash
   #在本地把项目打成jar包，传到服务器 /opt/docker/project 上
   cd /docker
   mkdir project
   vim appdockerfile
   
   #编写dockerfile
   FROM java:8u111
   COPY ./NCCommunity-0.0.1-SNAPSHOT.jar /tmp/app.jar
   EXPOSE 8080
   ENTRYPOINT java -jar /tmp/app.jar
   
   #制作镜像
   docker build -f appdockerfile -t ncc:1.0 .
   
   #运行镜像
   docker run --name ncc -p 8080:8080 -d ncc:1.0
   ```

   