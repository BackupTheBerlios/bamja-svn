==== Informations about the use of bamja_core ====

** More informations can you find at
** http://bamja.berlios.de

Bamja_core is an OSGi bundle and a subproject of bamja.

The bundle is a project of the Knopflerfish Eclipse plug-in.
See: http://knopflerfish.org/eclipse_plugin.html
This plugin assists the develop of this bandle.

This subproject (bundle) is a maven project too.
Maven assists the works for publication, but also the development 
outside eclipse.

=== Dependencies ===
- JavaSE 5, available from http://java.sun.com
- Knopflerfish 2, available from http://knopflerfish.org
	- the bundles kXML, cm and Log-Service from Knopflerfish 2

=== Running ===
Bamja_core is a OSGi bundle for the Knopflerfish framework and
used like such. You can be install and start it over a *.xargs file,
the Knopflerfish Desktop or over the Knopflerfish console.

You must start the required bundles too.

=== Maven ===
For the usage of maven you must insert the follow
repositories in the active profile in your settings.xml.

<repositories>
  <repository>
    <id>safehaus-repository</id>
    <name>Safehaus Repository</name>
    <url>http://m2.safehaus.org</url>
  </repository>
  <repository>
    <id>bamja-repository</id>
    <name>bamja repository</name>
    <url>http://bamja.berlios.de/maven2repository/</url>
  </repository>
</repositories>

=== Directories ===

 src			- the sourcedirectory of the bamja_core bundle 
			under the rules	for the standard directory layout
			for maven

 out			- the objectcode and the jar file of the bamja_core bundle
			(Will created by eclipse automatic)
 target			- the directory for the output of maven