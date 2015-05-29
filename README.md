jdcli
=====

Command line interface for reading javadocs. Text files are generated from java source files.

Setup
-----

- `mvn clean package`
- `source scripts/_jd_init.zsh`

Usage
-----

- `jd <class name>` opens the javadoc for the given class.
- `update-jd` updates the javadocs.

Configuration
-------------

Configuration is done in `~/.local/share/jdcli/config.json`. By default, jdcli will scan the jdk source zip (`%JAVA_HOME%/../src.zip`) and source artifacts in the local maven repo (`~/.m2/repository`). You can also configure terminal width for doc generation.
