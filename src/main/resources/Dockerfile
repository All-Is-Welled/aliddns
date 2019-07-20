FROM java:8

WORKDIR /usr/src/myapp

COPY *.jar ./

CMD ["java -jar aliddns-0.0.1.jar $Key $Secret $Domain $RRs"]