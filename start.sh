lsof -ti:8081 | xargs kill -9 2>/dev/null;
lsof -ti:8082 | xargs kill -9 2>/dev/null;
lsof -ti:8083 | xargs kill -9 2>/dev/null;

BASE_DIR=$(cd "$(dirname "$0")" && pwd)
export FILE_UPLOAD_DIR=${FILE_UPLOAD_DIR:-$BASE_DIR/uploads}
export FILE_PUBLIC_BASE_URL=${FILE_PUBLIC_BASE_URL:-https://api.linggong.tech}
mkdir -p "$FILE_UPLOAD_DIR"

cd c-service 
mvn clean package -Dmaven.test.skip=true
cd target
nohup java -jar c-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.additional-location=optional:file:../../config/c-service-env.yml > ../../logs/c-service.log 2>&1 &
cd ../../

cd enterprise-service 
mvn clean package -Dmaven.test.skip=true
cd target
nohup java -jar enterprise-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.additional-location=optional:file:../../config/enterprise-service-env.yml > ../../logs/enterprise-service.log 2>&1 &
cd ../../

cd platform-service 
mvn clean package -Dmaven.test.skip=true
cd target
nohup java -jar platform-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod --spring.config.additional-location=optional:file:../../config/platform-service-env.yml > ../../logs/platform-service.log 2>&1 &
cd ../../

cd enterprise-pc
npm install
npm run build
cd ../

cd platform-pc
npm install
npm run build
cd ../
