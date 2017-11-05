FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/grapdata.jar /grapdata/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/grapdata/app.jar"]
