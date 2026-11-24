# JavaEE8 / Tomcat 9 运行镜像；WAR 使用 quick-deploy 中的免构建产物
FROM tomcat:9.0.117-jdk11-temurin

ENV TZ=Asia/Shanghai
RUN rm -rf /usr/local/tomcat/webapps/*
COPY quick-deploy/music-web.war /usr/local/tomcat/webapps/music-web.war

EXPOSE 8080