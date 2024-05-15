FROM maven:3.8-openjdk-8

WORKDIR /workspace/app

COPY . .

USER root

RUN mvn clean package -Dmaven.test.skip

CMD java -jar /workspace/app/target/identity-management-0.0.5.jar