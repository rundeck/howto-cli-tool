package us.schueler.howto.detectors.markdown

import groovy.transform.CompileStatic
import spock.lang.Specification
import us.schueler.howto.model.DiscoveredAction

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
        actions[1].description == 'more\ndescription\n'
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
}
