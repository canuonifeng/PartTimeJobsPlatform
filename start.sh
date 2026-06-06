cd c-service 
mvn clean package -Dmaven.test.skip=true
cd target
nohup java -jar c-service-1.0.0-SNAPSHOT.jar > ../../logs/c-service.log 2>&1 &
cd ../../

cd enterprise-service 
mvn clean package -Dmaven.test.skip=true
cd target
nohup java -jar enterprise-service-1.0.0-SNAPSHOT.jar > ../../logs/enterprise-service.log 2>&1 &
cd ../../

cd platform-service 
mvn clean package -Dmaven.test.skip=true
cd target
nohup java -jar platform-service-1.0.0-SNAPSHOT.jar > ../../logs/platform-service.log 2>&1 &
cd ../../