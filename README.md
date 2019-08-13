SBT:
```
crossJS/run
crossJVM/run
crossJS/clean crossJS/fastOptJS moveJS pushJS
crossJVM/clean crossJVM/assembly crossJVM/docker
crossJVM/clean crossJVM/assembly crossJVM/dockerBuildAndPush
```

Server:
```
-Dpac.bot.token=""
-Dpac.bot.server="poku club"
-Dpac.bot.channel="art_challenge"
-Dpac.processor.mongo="mongodb://"
-Dpac.processor.database=""
-Dpac.thumbnailer.awsAccess=""
-Dpac.thumbnailer.awsSecret=""
-Dpac.processor.retryImages="true"
```

Heroku:
```
heroku container:login
docker push registry.heroku.com/wispy-cross/web
heroku container:release web --app wispy-cross
heroku open --app wispy-cross
heroku logs --tail --app wispy-cross
```