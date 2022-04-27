
# Welcome to cando

Cando lets you easily run predefined actions defined in a markdown-formatted text file - just like this one.

Define H2 headings for each action you want to define.

## Gradle: build

You can build the project easily with:

    ./gradlew build

alias
:   gradle:build

## Gradle: test
you can test with:

    ./gradlew check


alias
:   gradle:test

## Gradle: local install

You can build and install locally using:

    ./gradlew :app:installDist


alias
:   gradle:local-install

## Run local install

You can run the local install of this project using (cando-ception)

    sh app/install/dist/cando/bin/cando "$@"

alias
:   local
