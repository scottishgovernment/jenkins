package build

class Artifact {

  /* Name of the Debian package built by this job */
  String debian

  /* Maven coordinates, in the form groupId:artifactId  */
  String maven

  /* Host name (without environment prefix) on which to deploy builds */
  List<String> hosts

}
