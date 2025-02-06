package org.rundeck.howto.detectors.markdown


import spock.lang.Specification
import org.rundeck.howto.model.DiscoveredAction

class MarkdownDetectorSpec extends Specification {
    def "parse basic actions"() {
        given:
        String doc = '''# header
ignored

## action1

description

    action words

## action 2

more

description

`another action`

## action 3

blah

```
more action
```
'''
        List<DiscoveredAction> actions = []
        when:
        MarkdownDetector.parseActions(new ByteArrayInputStream(doc.bytes), null, actions)
        then:
        actions.size() == 3
        actions[0].name == 'action1'
        actions[0].invocationString == 'action words\n'
        actions[0].description == 'description\n'
        actions[1].name == 'action-2'
        actions[1].invocationString == 'another action'
        actions[1].description == 'more\ndescription\n`another action`\n'
        actions[2].name == 'action-3'
        actions[2].invocationString == 'more action\n'
        actions[2].description == 'blah\n'
    }

    def "parse h1 header pattern"() {
        given:
        String doc = """# header
ignored

## action1

description

    action words

# ${h1text}

## action 2

more

description

`another action`

## action 3

blah

```
more action
```

# ${h1text2}

## something else

`blah`
"""
        List<DiscoveredAction> actions = []
        when:
        MarkdownDetector.parseActions(new ByteArrayInputStream(doc.bytes), pattern, actions)
        then:
        actions*.name == expect
        where:
        h1text                   | h1text2        | pattern                           | expect
        'howto'                  | 'other'        | MarkdownDetector.HOWTO_H1_PATTERN | ['action-2', 'action-3']
        'HOWTO'                  | 'other'        | MarkdownDetector.HOWTO_H1_PATTERN | ['action-2', 'action-3']
        'howto use this project' | 'other'        | MarkdownDetector.HOWTO_H1_PATTERN | ['action-2', 'action-3']
        'how to'                 | 'other'        | MarkdownDetector.HOWTO_H1_PATTERN | ['action-2', 'action-3']
        'how-to'                 | 'other'        | MarkdownDetector.HOWTO_H1_PATTERN | ['action-2', 'action-3']
        'How To'                 | 'other'        | MarkdownDetector.HOWTO_H1_PATTERN | ['action-2', 'action-3']
        'How To: do stuff'       | 'other'        | MarkdownDetector.HOWTO_H1_PATTERN | ['action-2', 'action-3']
        'Project How-To'         | 'other'        | MarkdownDetector.HOWTO_H1_PATTERN | ['action-2', 'action-3']
        'unrelated'              | 'other'        | MarkdownDetector.HOWTO_H1_PATTERN | []
        'unrelated'              | 'howto'        | MarkdownDetector.HOWTO_H1_PATTERN | ['something-else']
        'howto: project'         | 'howto: other' | MarkdownDetector.HOWTO_H1_PATTERN | ['action-2', 'action-3', 'something-else']
    }

    def "example"() {
        given:
        String doc = """# header
ignored

# howto do something else

ignored

# bbq

ignored

## action1

description

    action words

# other

## action 2

more

description

`another action`

## action 3

blah

```
more action
```

# blah

## something else

`blah`
"""
        List<DiscoveredAction> actions = []
        when:
        MarkdownDetector.parseActions(new ByteArrayInputStream(doc.bytes), null, actions)
        then:
        actions*.name == expect
        where:
        expect = ['action1', 'action-2', 'action-3', 'something-else']
    }

    def "test p tag before h1"(){
        given:
        String doc ="""<p align="center">
<a href="https://www.rundeck.com">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://www.rundeck.com/hubfs/Pager%20Duty%20Branding/RundeckbyPagerDutyDM.svg" width="300">
    <source media="(prefers-color-scheme: light)" srcset="https://www.rundeck.com/hubfs/Pager%20Duty%20Branding/RundeckbyPagerDuty.svg" width="300">
    <img alt="Rundeck">
  </picture>
</a>
</p>

<h3 align="center">Execute workflows across your existing automations<br /> or quickly automate previously manual procedures.</h3>

<br />
<p align="center">
<a href="https://github.com/rundeck/rundeck/"><img src="https://img.shields.io/github/stars/rundeck/rundeck?style=social" alt="GitHub Stars"></a>
<a href="https://github.com/rundeck/rundeck/releases/latest"><img src="https://img.shields.io/github/release/rundeck/rundeck.svg" alt="Latest release"></a>

<div align="center">

| Deb                                           | RPM                                           | War                                           |
| --------------------------------------------- | --------------------------------------------- | --------------------------------------------- |
| [Download](https://www.rundeck.com/downloads) | [Download](https://www.rundeck.com/downloads) | [Download](https://www.rundeck.com/downloads) |

</div>

<br />

Rundeck by PagerDuty is an open source runbook automation service with a web console, command line tools and a WebAPI. It lets you easily standardize tasks to improve operational quality by deploying automation across a set of nodes.

- [Visit the Website](https://www.rundeck.com)

- [Read the latest documentation](https://docs.rundeck.com/docs/)

- [Get help from the Community](https://community.pagerduty.com/ask-a-product-question-2)

- [Install Rundeck](https://docs.rundeck.com/docs/administration/install/installing-rundeck.html)

<br />

See the [Release Notes](https://docs.rundeck.com/docs/history/) for the latest version information.

<br />

# How To Build:

Primary build is supported with gradle. More info in the [wiki](https://github.com/rundeck/rundeck/wiki/Building-and-Testing).

Requirements: Java 11, NodeJs 18

## Build with Gradle

Produces: `rundeckapp/build/libs/rundeck-X.Y.war`

    ./gradlew build

## Docker Build

Uses the war artifact and produces a docker image.

Creates image `rundeck/rundeck:SNAPSHOT`, you can define `-PdockerTags` to add additional tags

    ./gradlew :docker:officialBuild

<br />

# Documentation

Available online at <https://docs.rundeck.com/docs>

FAQ: <https://github.com/rundeck/rundeck/wiki/FAQ>

<br />

# Development

Refer to the [IDE Development Environment](https://github.com/rundeck/rundeck/wiki/IDE-Development-Environment) to get set up using IntelliJ IDEA or Eclipse/STS.

- [Issue tracker](https://github.com/rundeck/rundeck/issues) at github.com

Do you have changes to contribute? Please see the [Development](https://github.com/rundeck/rundeck/wiki/Development) wiki page.

<br />

# License

Copyright 2024 PagerDuty, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License."""
        List<DiscoveredAction> actions = []
        when:
        MarkdownDetector.parseActions(new ByteArrayInputStream(doc.bytes), null, actions)
        then:
        actions*.name == expect
        where:
        expect = ['build-with-gradle', 'docker-build']
    }
    def "test text before h1"(){
        given:
        String doc ="""
some text

# How To Build:

Primary build is supported with gradle. More info in the [wiki](https://github.com/rundeck/rundeck/wiki/Building-and-Testing).

Requirements: Java 11, NodeJs 18

## Build with Gradle

Produces: `rundeckapp/build/libs/rundeck-X.Y.war`

    ./gradlew build

## Docker Build

Uses the war artifact and produces a docker image.

Creates image `rundeck/rundeck:SNAPSHOT`, you can define `-PdockerTags` to add additional tags

    ./gradlew :docker:officialBuild

<br />

# Documentation

Available online at <https://docs.rundeck.com/docs>

FAQ: <https://github.com/rundeck/rundeck/wiki/FAQ>

<br />

# Development

Refer to the [IDE Development Environment](https://github.com/rundeck/rundeck/wiki/IDE-Development-Environment) to get set up using IntelliJ IDEA or Eclipse/STS.

- [Issue tracker](https://github.com/rundeck/rundeck/issues) at github.com

Do you have changes to contribute? Please see the [Development](https://github.com/rundeck/rundeck/wiki/Development) wiki page.

<br />

# License

Copyright 2024 PagerDuty, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License."""
        List<DiscoveredAction> actions = []
        when:
        MarkdownDetector.parseActions(new ByteArrayInputStream(doc.bytes), null, actions)
        then:
        actions*.name == expect
        where:
        expect = ['build-with-gradle', 'docker-build']
    }
}
