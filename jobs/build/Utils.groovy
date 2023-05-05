package build

import javaposse.jobdsl.dsl.helpers.ScmContext
import java.util.regex.*

class Utils {

    static def repo(name) {
        return "ssh://git@git.digital.gov.uk/mgv/" + name + ".git"
    }

    static def slug(text) {
        return text.toLowerCase().replaceAll(' ', '-')
    }

    static def trim(str) {
        def leadingWhitespace = Pattern.compile("\\s*")
        def matcher = leadingWhitespace.matcher(str)
        if (matcher.lookingAt()) {
            return str.replaceAll(Pattern.quote(matcher.group()), "")
        }
        return str;
    }

    static def awsRepo(def ScmContext delegate) {
        delegate.git {
            remote {
                name('origin')
                url(repo('aws'))
            }
            branch('refs/heads/master')
        }
    }

    static def puppetRepo(def ScmContext delegate) {
        delegate.git {
            remote {
                name('origin')
                url(repo('puppet'))
            }
            branch('refs/heads/master')
        }
    }
}
