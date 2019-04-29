FROM java:8

ENV SPRING_PROFILES_ACTIVE production

EXPOSE 8081

CMD java -jar /data/app.jar

ADD target/identity-management-0.0.5.jar /data/app.jar
