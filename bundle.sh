#!/bin/bash

temp=build/mini-uav
version=`gradle -q printVersion`

echo "Log created at $tempLog"
rm -R $temp
mkdir -p $temp
shopt -s extglob 

# Linux
echo 'Bundling Linux package...'
gradle shadowJar
tempLinux=$temp/linux 
mkdir $tempLinux
cd $tempLinux
cp build/libs/MiniUAV.jar .
echo '#!/bin/bash 
java -jar MiniUAV.jar $@' > ./MiniUAV.sh
chmod +x ./MiniUAV.sh
cp -r drones .
cp -r music .
cp config.yaml .
cp icon.ico .
tar -czvf mini-uav-$version-linux.tar.gz *
rm -R -- !(mini-uav-$version-linux.tar.gz)
cd ../../../
echo 'ok.'

# Windows
echo 'Bundling Windows package...'
gradle shadowJar
tempWindows=$temp/windows
mkdir $tempWindows
cd $tempWindows
cp build/libs/MiniUAV.jar .
cp -r drones .
cp config.yaml .
cp icon.ico .
cp lib/* .
tar -czvf mini-uav-$version-windows.tar.gz *
rm -R -- !(mini-uav-$version-windows.tar.gz)
cd ../../../
echo 'ok.'

# Bundle release
echo 'Bundling release...'
cd $temp
tar -czvf mini-uav-$version.tar.gz *
echo 'ok.' | tee -a $tempLog

echo 'App bundled Successfully!'

