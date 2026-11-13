# JavaEE8 + Tomcat9 镜像部署 WAR（实验五 Docker 路径）
FROM tomcat:9-jdk11

# 清掉默认示例应用，避免上下文冲突
RUN rm -rf /usr/local/tomcat/webapps/*

# 部署本工程 WAR（先 mvn clean package 产出）
COPY target/music-lab.war /usr/local/tomcat/webapps/music-lab.war

# 数据库连接通过环境变量外置（DbUtil 优先读 DB_URL/DB_USERNAME/DB_PASSWORD）
ENV DB_URL="" DB_USERNAME="" DB_PASSWORD=""

EXPOSE 8080
CMD ["catalina.sh", "run"]