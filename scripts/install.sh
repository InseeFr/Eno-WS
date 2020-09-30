#!/usr/bin/env bash

set -e

BRANCH_NAME="$1"
ENO_CORE_URL="https://github.com/InseeFr/Eno.git"
LUNATIC_MODEL_URL="https://github.com/InseeFr/Lunatic-Model.git"

function install_eno(){
    bash scripts/gh2mvn.sh "$ENO_CORE_URL" "$BRANCH_NAME"
}

function install_lunatic_model(){
    bash scripts/gh2mvn.sh "$LUNATIC_MODEL_URL" "$BRANCH_NAME"
}

function main(){
    install_eno
    install_lunatic_model
}

main