#!/usr/bin/env bash
#title           :install_from_vch.sh
#description     :This script installs files stored on github to local mvn repo
#author		     :a-cordier
#==============================================================================

set -e

GH_URL="$1"
BRANCH_NAME="$2"
FOLDER=$(basename -s .git $GH_URL)

function get_sources(){
    TARGET=$(mktemp -d)
    git clone --depth=1 --branch="$BRANCH_NAME" "$GH_URL" "$FOLDER"
}

function install_files(){
    cd "$FOLDER"
    mvn clean package install -DskipTests
    cd .. && rm -rf "$FOLDER"
}

function main(){
    get_sources && install_files
}

main