
# Welcome to Howto

Howto lets you easily discover how to do "actions" within a source repo, such as build, test, install, etc.
It can discover and run predefined actions from a variety of sources.

You can easily define actions a markdown-formatted text file just like this one.

Howto can also detect actions using common build systems, such as NPM, Gradle, etc, and provide shortcuts to invoking them.

## Build

Build the project with Gradle

    ./gradlew build

## Test

Test the project with Gradle

    ./gradlew check

## Install Locally

Build and install locally with Gradle.

This installs within the app/build/install/how/bin directory.

    ./gradlew :app:installDist

## Run Locally

Run local installation

    ./app/build/install/how/bin/how "$@"

## Release

Release the project.

    ./gradlew release

## Pond

Toss a coin in the pond!

    #!/bin/bash
    echo "Today you will have:"
    echo
    case "$(( $RANDOM % 4 ))" in
    0) echo "A little luck.";;
    1) echo "Good luck." ;;
    2) echo "Great luck.";;
    3) echo "Big trouble.";;
    esac