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

cd enterprise-pc
npm run build
cd ../

cd enterprise-uniapp
npm run build:h5
cd ../

cd platform-pc
npm run build
cd ../

cd worker-uniapp
npm run build:h5
cd ../