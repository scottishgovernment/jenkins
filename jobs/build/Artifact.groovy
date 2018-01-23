package build

class Artifact {

  /* Name of the Debian pacakge built by this job */
  String debian

  /* Maven coordinates, in the form groupId:artifactId  */
  String maven

  /* Host name (without environment prefix) on which to deploy builds */
  String host

}
