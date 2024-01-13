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
cp build/libs/MiniUAV.jar $tempLinux
echo '#!/bin/bash 
java -jar MiniUAV.jar $@' > $tempLinux/MiniUAV.sh
chmod +x $tempLinux/MiniUAV.sh
cp -r drones $tempLinux
cp -r music $tempLinux
cp config.yaml $tempLinux
cp icon.ico $tempLinux
cd $tempLinux
tar -czvf mini-uav-$version-linux.tar.gz *
rm -R -- !(mini-uav-$version-linux.tar.gz)
cd ../../../
echo 'ok.'

# Windows
echo 'Bundling Windows package...'
gradle shadowJar
tempWindows=$temp/windows
mkdir tempWindows
cp build/libs/MiniUAV.jar tempWindows
cp -r drones tempWindows
cp config.yaml tempWindows
cp icon.ico tempWindows
cp lib/* $tempWindows
cd tempWindows
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

