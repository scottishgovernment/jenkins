import java.util.regex.*
leadingWhitespace = Pattern.compile("^ *", Pattern.MULTILINE)

def repo(name) { return "ssh://git@stash.digital.gov.uk:7999/mgv/" + name + ".git" }
def strip(str) { return leadingWhitespace.matcher(str).replaceAll(""); }

javaTemplate='''\
  version="1.0.${BUILD_ID}"

  git clean -fdx
  git update-ref --no-deref HEAD HEAD
  mvn -B versions:set versions:use-latest-versions \\
    -DnewVersion="${version}" \\
    -Dincludes='org.mygovscot.*,scot.mygov.*' \\
    -DgenerateBackupPoms=false
  git commit -am "Set version to ${version}"
  git tag -a -m "Build ${version}" ${version}

  mvn verify
  #git push --tags ssh://git@stash.digital.gov.uk:7999/mgv/${repo}.git "${version}"
  #mvn -B -Prelease verify sonar:sonar deploy

  #git checkout HEAD^
  #mvn -B deploy -DskipTests
'''

def java(name) {
  return "set -ex\nrepo=${name}\n" + strip(javaTemplate);
}

job("beta-config") {
  scm {
    git(repo("beta-config"))
  }
  steps {
    shell(java("beta-config"))
  }
}

job("validation") {
  scm {
    git(repo("unified_validation"))
  }
  steps {
    shell(java("unified_validation"))
  }
}

job("versions") {
  steps {
    shell("cd ../../mygov-seed/workspace")
    shell(readFileFromWorkspace('set-build-id'))
  }
}
