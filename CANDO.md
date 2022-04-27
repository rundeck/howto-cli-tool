
# Welcome to cando

Cando lets you easily run predefined actions from a variety of sources.

You can easily define actions a markdown-formatted text file just like this one.

Cando can also look for common build systems, such as NPM, Gradle, etc, and provide shortcuts to invoking them, if you want.

Define H2 headings for each action you want to define.

## build

Build the project with Gradle

    ./gradlew build

## test

Test the project with Gradle

    ./gradlew check

## install

Build and install locally with Gradle

    ./gradlew :app:installDist

## local

Run local installation

    ./app/build/install/cando/bin/cando "$@"

## pond

Toss a coin in the pond!

    #!/bin/bash
    echo "Today you will have:\n"
    R=$(( $RANDOM % 4 ))
    [ $R = 0 ] && echo "A little luck."
    [ $R = 1 ] && echo "Good luck."
    [ $R = 2 ] && echo "Great luck."
    [ $R = 3 ] && echo "Big trouble."