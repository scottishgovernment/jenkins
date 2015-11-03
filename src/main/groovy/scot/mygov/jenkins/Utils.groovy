package scot.mygov.jenkins

import java.util.regex.*

class Utils {

    static def trim(str) {
        def leadingWhitespace = Pattern.compile("\\s*")
        def matcher = leadingWhitespace.matcher(str)
        if (matcher.lookingAt()) {
            return str.replaceAll(Pattern.quote(matcher.group()), "")
        }
        return str;
    }

    static def repo(name) {
        return "ssh://git@stash.digital.gov.uk:7999/mgv/" + name + ".git"
    }

}
