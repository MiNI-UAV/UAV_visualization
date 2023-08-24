#!/bin/bash

temp=build/mini-uav
version=`gradle -q printVersion`
tempLog=`mktemp -t mini-uav-bundle-log.XXXXXX`

echo "Log created at $tempLog" | tee -a $tempLog
rm -R $temp >> $tempLog 2>&1
mkdir -p $temp
shopt -s extglob 

# Linux
echo 'Bundling Linux package...' | tee -a $tempLog
gradle shadowJar >> $tempLog 2>&1
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
tar -czvf mini-uav-$version-linux.tar.gz * >> $tempLog 2>&1 
rm -R -- !(mini-uav-$version-linux.tar.gz)
cd ../../../
echo 'ok.' | tee -a $tempLog

# Windows
echo 'Bundling Windows package...' | tee -a $tempLog
gradle createExe >> $tempLog 2>&1
tempWindows=$temp/windows 
mkdir $tempWindows
cp -r build/launch4j/* $tempWindows
cp -r drones $tempWindows
cp -r music $tempWindows
cp config.yaml $tempWindows
cp icon.ico $tempWindows
cp lib/* $tempWindows
cd $tempWindows
tar -czvf mini-uav-$version-windows.tar.gz * >> $tempLog 2>&1
rm -R -- !(mini-uav-$version-windows.tar.gz)
cd ../../../
echo 'ok.' | tee -a $tempLog

# Bundle release
echo 'Bundling release...' | tee -a $tempLog
cd $temp
tar -czvf mini-uav-$version.tar.gz * >> $tempLog 2>&1
echo 'ok.' | tee -a $tempLog

echo 'App bundled Successfully!' | tee -a $tempLog

