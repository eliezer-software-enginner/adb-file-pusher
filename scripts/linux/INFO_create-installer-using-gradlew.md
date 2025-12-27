

##Forma manual

./gradlew clean jar

$ ls build/libs/

./scripts/linux/create-installer-using-gradlew.sh

## Forma automatizada utilizando a task criada lá no gradle
./gradlew createInstaller

--------------------------------
Como instalar aplicativos .deb

sudo dpkg -i dist/myapp_1.2_amd64.deb

projeto atual: adb-file-pusher_1.0.0_amd64.deb

sudo dpkg -i dist/adb-file-pusher_1.0.0_amd64.deb