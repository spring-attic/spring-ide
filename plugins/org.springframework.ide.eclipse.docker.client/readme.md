Spotify Docker Client
=====================

Repackaged as a single osgi bundle with all its dependencies.

Packaging Process:
==================

- Build the maven project `spotify-docker-client-wrapper` which is included as a sub-folder in this project.
- copy all the jars from `spotify-docker-client-wrapper/target/dependency` to `dependency`.
- Open `manifest.mf` with PDE tabbed editor. Select the 'Runtime' tab and then:
   - clear out exported packages section then add all com.spotify.docker.client* packages back
   - clear our 'classpath' section and all jars in the 'dependency folder.


IMPORTANT:
   
The following dependencies must not be consumed from wrapped jars and should instead be replaced with
bundle dependencies. This is to avoid classloader errors because of two versions of the same types clashing
with eachother in boot.dash.docker plugin.

- guava 

Process to replace the dependency is to 

- remove it from the 'classpath' section of the manifest
- add equivalent bundle dependency. Try to preserve the minimum version constraint.