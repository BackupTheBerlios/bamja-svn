==== Informations about the use of bamja ====

** More informations can you find at
** http://bamja.berlios.de

Bamja is a collection of OSGi bundles.
Every bundle is be located in a own subdirectory and is a subproject
of bamja.

The bundles are projects of the Knopflerfish Eclipse plug-in.
See: http://knopflerfish.org/eclipse_plugin.html
This plugin assists the develop of the bandles.

The complete bamja project is a maven2 project with a module for
every subproject of bamja. Therewith every subproject (bundle) is 
a maven project too.
Maven assists the works for publication, but also the development 
outside eclipse.

=== Dependencies ===
- JavaSE 5, available from http://java.sun.com
- Knopflerfish 2, available from http://knopflerfish.org
	- the bundles kXML, cm and Log-Service from Knopflerfish 2

=== Running ===
The subprojects of bamja are OSGi bundles for the Knopflerfish
framework and used like such. You can be install and start it 
over a *.xargs file, the Knopflerfish Desktop or over the
Knopflerfish console.

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

 bamja_core		- the core bundle of bamja (subproject)
 src			- files for maven

 target			- the directory for the output of maven