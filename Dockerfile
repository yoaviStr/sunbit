FROM docker.ouroath.com:4443/adoptopenjdk:11-jre-hotspot

ENV COMPONENT addressbook

COPY target/${COMPONENT}*.jar /${COMPONENT}.jar

WORKDIR /${COMPONENT}

ENTRYPOINT ["java","-jar","/addressbook.jar"]
