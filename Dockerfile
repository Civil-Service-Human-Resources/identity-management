FROM java:8

ENV SPRING_PROFILES_ACTIVE production

EXPOSE 8081

ADD target/identity-management-0.0.5.jar /data/app.jar

ADD lib/AI-Agent.xml /opt/appinsights/AI-Agent.xml
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.0.3/applicationinsights-agent-3.0.3.jar /opt/appinsights/applicationinsights-agent-3.0.3.jar

CMD java -javaagent:/opt/appinsights/applicationinsights-agent-3.0.3.jar -jar /data/app.jar
