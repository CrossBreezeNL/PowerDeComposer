docker run --rm -it --name PowerDeComposer -v ${PWD}/PowerDeComposer:/usr/src/mymaven -v ${PWD}/.m2/:/usr/share/maven/ref -w /usr/src/mymaven maven:3.8.6-openjdk-11 mvn clean package assembly:single -PexecutableJar