FROM amazoncorretto:25-alpine

ENV SPRING_PROFILES_ACTIVE production

EXPOSE 8081

ADD target/identity-management-0.0.5.jar /data/app.jar

ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.7.10/applicationinsights-agent-3.7.10.jar /opt/appinsights/applicationinsights-agent.jar

CMD java -javaagent:/opt/appinsights/applicationinsights-agent.jar -jar /data/app.jar
