# Project Starter

## Build and Run Project

```
interview-tech-challenge> mvn clean package

interview-tech-challenge-main> sh ./localBuildAndRun.sh

...
[main] INFO org.lotlinx.interview.MainServerVerticle - MainVerticle started
[vert.x-eventloop-thread-0] INFO org.lotlinx.interview.MainServerVerticle - HTTP server started on port 8080
```

# Test Endpoint

```
➜  Workspace curl http://localhost:8080/hello
Hello from Lotlinx!
```

# Example Endpoint
```terminaloutput
 curl http://localhost:8080/getCurrentAirPollution\?latitude\=-1\&longitude\=-1
{
  "coord" : {
    "lon" : -1,
    "lat" : -1
  },
  "list" : [ {
    "main" : {
      "aqi" : 2
    },
    "components" : {
      "co" : 179.37,
      "no" : 0,
      "no2" : 0.01,
      "o3" : 83.25,
      "so2" : 0.16,
      "pm2_5" : 3.9,
      "pm10" : 10.88,
      "nh3" : 0.22
    },
    "dt" : 1757607978
  } ]
}%

```