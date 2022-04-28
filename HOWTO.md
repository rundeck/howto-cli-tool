
# Welcome to Howto

Howto lets you easily discover how to do "actions" within a source repo, such as build, test, install, etc.
It can discover and run predefined actions from a variety of sources.

You can easily define actions a markdown-formatted text file just like this one.

Howto can also detect actions using common build systems, such as NPM, Gradle, etc, and provide shortcuts to invoking them.

# Writing a Howto doc

Create a "howto.md" or a `# Howto` section in your readme.md.

Define H2 headings for each action you want to define. The heading will be the name of the action.

    ## Build

Within the H2 section, use a code block (indented of fenced), or simple code section to define the action to invoke.

    ```
    make dist
    ```

If you want to accept arguments use `"${@}"` (for bash) 

    `sh build.sh "${@}"`

# Using other build tools

If no "howto.md" or `# Howto` section in the readme is found,
Howto will look for common build tools, and try to introspect them.

Currently these tools are supported:

- [x] NPM - looks for the `package.json` file and any "scripts" defined within it

TODO:

- [ ] Gradle
- [ ] Maven
- [ ] Your favorite build tool...

## Build

Build the project with Gradle

    ./gradlew build

## Test

Test the project with Gradle

    ./gradlew check

## Install

Build and install locally with Gradle.

This installs within the app/build/install/how/bin directory.

    ./gradlew :app:installDist

## Local

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