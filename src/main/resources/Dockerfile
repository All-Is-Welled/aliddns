FROM java:8

WORKDIR /usr/src/myapp

COPY *.jar ./

ENV Key=Key

ENV Secret=Secret

ENV Domain=example.com

ENV RRs=demo,demo1,demo2,@

ENV Timeout=5

ENTRYPOINT java -jar aliddns-0.0.1.jar $Key $Secret $Domain $RRs $Timeout