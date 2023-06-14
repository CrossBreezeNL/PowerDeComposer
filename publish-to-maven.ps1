# In order for PGP to work you need PGP_TTY environment variable set correctly (see https://www.gnupg.org/documentation/manuals/gnupg/Invoking-GPG_002dAGENT.html).
docker run --rm -it --name PowerDeComposer -v ${PWD}/PowerDeComposer:/usr/src/mymaven -v ${PWD}/.m2/:/usr/share/maven/ref -v $Env:APPDATA\gnupg:/root/.gnupg -w /usr/src/mymaven maven:3.8.6-openjdk-11 /bin/bash -c 'export GPG_TTY="$( tty )" && mvn clean deploy -Prelease'
