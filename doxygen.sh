#!/bin/bash

if [ -z "${MAXINE}" ]; then
  echo "MAXINE is not defined. It must point to a maxine repository directory."
  exit 1;
fi
if [ -z "${GRAAL}" ]; then
  echo "GRAAL is not defined. It must point to a maxine repository directory."
  exit 1;
fi

# Resolve location of this script
me="${BASH_SOURCE[0]}"
while [ -h "$me" ]; do
    me=`readlink -e "$me"`
done
script_home=$(cd `dirname $me`; pwd)/doc/doxygen

echo "script home: $script_home"
echo "removing temp dirs"

rm -r $script_home/src
rm -r $script_home/html
rm -r $script_home/latex

echo "collecting sources"
mkdir -p $script_home/src
cp -r $GRAAL/graal/GraalCompiler/src/* $script_home/src/
cp -r $GRAAL/graal/GraalGraph/src/* $script_home/src/
cp -r $MAXINE/CRI/src/* $script_home/src/

echo "preparing sources"
find $script_home/src/ -type f -print0 | xargs -0 sed -i 's/{@code \([^}]*\)}/\1/g'
find $script_home/src/ -type f -print0 | xargs -0 sed -i 's/{@code/ /g'

pushd $script_home
echo "running doxygen"
doxygen ../graal.doxy > out.txt 2> err.txt
cat err.txt | grep -v "unable to resolve link" | grep -v "expected whitespace" | grep -v ACCESSOR | grep -v "not documented" > errors.txt
rm err.txt
popd
