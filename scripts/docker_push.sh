#!bin/bash
docker login -u $DOCKER_USER -p $DOCKER_TOKEN
docker build -t $DOCKER_USER/eno-ws:$TRAVIS_BRANCH .
docker push $DOCKER_USER/eno-ws:$TRAVIS_BRANCH
