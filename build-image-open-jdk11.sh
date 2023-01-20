# build jar
mvn clean && mvn package

# build docker image
docker build -t javbus-tg-bot:v1.0.0 .

#tag name
docker tag javbus-tg-bot:v1.0.0 liuzy/javbus-tg-bot:latest

# push to docker hub
docker push liuzy/javbus-tg-bot:latest

# clean docker images
# shellcheck disable=SC2006
imagesId=`docker images | grep javbus-tg-bot | awk '{print $3}'`
echo $imagesId
for str in $imagesId
do
  docker rmi $str -f
done
