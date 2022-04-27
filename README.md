# Cando

You can do it!

What can I do?

This tool provides insight into "What can I dd?" with a software project.

Just checked out some repo? `cando` lists available actions, such as how to build it, install it, test it, etc.

```bash

# what happens if you run cando on this repo 
$ git clone https://github.com/gschueler/cando
$ cd cando
$ cando
cando 1.1 - detected action types: gradle, cando
> 1. Gradle: build
> 2. Gradle: test
> 3. Gradle: local install
> 4. Cando: run local install
> 5. Cando: Help
Choice [5]: 5
Running: cando help

# Welcome to cando

## Gradle: build

You can build the project easily with:

    ./gradlew build

alias: cando run gradle:build

## Gradle: test
you can test with:

    ./gradlew check

alias: cando run gradle:test

## Gradle: local install

You can build and install locally using:

    ./gradlew :app:installDist

alias: cando run gradle:local-install

## Cando: run local install

You can run the local install of this project using (cando-ception)

    cando run local

## Cando: Help

Get Help with Cando

    cando help