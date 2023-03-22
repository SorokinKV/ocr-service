# FROM openjdk11:jdk-11.0.11_9-ubuntu-slim
FROM openjdk11:jdk-11.0.11_9-ubuntu-slim-tesseract
ENV TZ=Europe/Moscow
COPY ./certs/rootCA.crt /usr/local/share/ca-certificates/rootCA.crt
RUN keytool -noprompt -import -file /usr/local/share/ca-certificates/rootCA.crt -alias sorokinkv.local -keystore /root/truststore.jks -storepass changeit
RUN update-ca-certificates
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-server","-Xms1024m","-Xmx8192m","-jar","-Dspring.profiles.active=test","-Dspring-boot.run.jvmArguments=-Xms1024m -Xmx8192m","-Djavax.net.ssl.trustStorePassword=changeit","-Djavax.net.ssl.trustStore=/root/truststore.jks","-Djavax.net.ssl.trustStoreType=jks","/app.jar"]
