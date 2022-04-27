
# Welcome to cando

Cando lets you easily run predefined actions from a variety of sources.

You can easily define actions a markdown-formatted text file just like this one.

Cando can also look for common build systems, such as NPM, Gradle, etc, and provide shortcuts to invoking them, if you want.

Define H2 headings for each action you want to define. The heading will be the name of the action.

Within the H2 section, use a code block, or code section to define the action to invoke.

If you want to accept arguments use `"${@}"` (for bash) 

## Build

Build the project with Gradle

    ./gradlew build

## Test

Test the project with Gradle

    ./gradlew check

## Install

Build and install locally with Gradle

    ./gradlew :app:installDist

## Local

Run local installation

    ./app/build/install/cando/bin/cando "$@"

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