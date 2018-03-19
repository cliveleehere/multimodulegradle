#!/usr/bin/env bash
if [[ ! -f settings.gradle ]] ; then
    echo 'Make sure to run this at the root of your project'
    exit 1
fi

find . -iname build.gradle -not -path ./build.gradle  | while read build_file; do
    moduledir=$(dirname $build_file)
    modulename=$(basename $moduledir)
    new_build_file="$moduledir/$modulename.gradle"
    echo "Renaming'$build_file' to $new_build_file"
    git mv $build_file $new_build_file
done