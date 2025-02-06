package org.rundeck.howto


import spock.lang.Specification


class HowtoSpec extends Specification {
    def "camelcase abbreviation match"() {
        expect:
        Howto.abbreviationCamelcase(abbrev, name) == matches
        where:
        name                | abbrev  | matches
        'some-action'       | ''      | false
        'some-action'       | 'sA'    | true
        'some-action'       | 'sa'    | true
        'some-action'       | 's'     | true
        'some-action'       | 'sb'    | false
        'some-action'       | 'sB'    | false
        'some-action'       | 's-a'   | false
        'some-action'       | 's-b'   | false
        'some-other-action' | 'sOA'   | true
        'some-other-action' | 's-o-a' | false

    }

    def "hyphenated abbreviation match"() {
        expect:
        Howto.abbreviatedHyphenated(abbrev, name) == matches
        where:
        name                | abbrev  | matches
        'some-action'       | ''      | false
        'some-action'       | 'sA'    | false
        'some-action'       | 's-A'   | true
        'some-action'       | 'sa'    | false
        'some-action'       | 's-a'   | true
        'some-action'       | 's'     | true
        'some-action'       | 'sb'    | false
        'some-action'       | 's-b'   | false
        'some-action'       | 's-B'   | false
        'some-other-action' | 'sOA'   | false
        'some-other-action' | 's-o-a' | true
        'some-other-action' | 's-o'   | true

    }
}
