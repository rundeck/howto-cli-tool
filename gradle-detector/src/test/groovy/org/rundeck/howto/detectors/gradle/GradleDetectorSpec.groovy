package org.rundeck.howto.detectors.gradle


import spock.lang.Specification

import java.util.regex.Matcher
import java.util.regex.Pattern


class GradleDetectorSpec extends Specification {
    static String GRADLE_OUT_1 = '''> Task :tasks

------------------------------------------------------------
Tasks runnable from root project 'howto'
------------------------------------------------------------

Application tasks
-----------------
run - Runs this project as a JVM application

Build tasks
-----------
assemble - Assembles the outputs of this project.
build - Assembles and tests this project.
buildDependents - Assembles and tests this project and all projects that depend on it.
buildNeeded - Assembles and tests this project and all projects it depends on.
classes - Assembles main classes.
clean - Deletes the build directory.
jar - Assembles a jar archive containing the main classes.
testClasses - Assembles test classes.

Build Setup tasks
-----------------
init - Initializes a new Gradle build.
wrapper - Generates Gradle wrapper files.

BuildConfig tasks
-----------------
generateBuildConfig - Generates the build constants class for 'main' source
generateTestBuildConfig - Generates the build constants class for 'test' source
'''

    def "task match"() {
        given:

        Pattern p = GradleDetector.TASK_PATTERN
        when:
        Matcher m = p.matcher(input)
        then:
        m.matches() == match
        m.groupCount() == count
        if (m.groupCount()) {
            m.group(1) == m1
            m.group(2) == m2
        }
        where:
        input                 | match | count | m1     | m2
        'asdf - asdf fafasdf' | true  | 2     | 'asdf' | 'asdf fafasdf'
    }

    def "parse gradle tasks output"() {
        given:

        def reader = new GradleDetector.LineReader()
        when:

        GRADLE_OUT_1.readLines().each { reader.accept(it) }
        then:

        reader.found == expected

        where:
        expected = [
                'run'                    : 'Runs this project as a JVM application',
                'assemble'               : 'Assembles the outputs of this project.',
                'build'                  : 'Assembles and tests this project.',
                'buildDependents'        : 'Assembles and tests this project and all projects that depend on it.',
                'buildNeeded'            : 'Assembles and tests this project and all projects it depends on.',
                'classes'                : 'Assembles main classes.',
                'clean'                  : 'Deletes the build directory.',
                'jar'                    : 'Assembles a jar archive containing the main classes.',
                'testClasses'            : 'Assembles test classes.',
                'init'                   : 'Initializes a new Gradle build.',
                'wrapper'                : 'Generates Gradle wrapper files.',
                'generateBuildConfig'    : 'Generates the build constants class for \'main\' source',
                'generateTestBuildConfig': 'Generates the build constants class for \'test\' source',
        ]

    }
}
