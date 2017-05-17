The source code in this package is taken from here:

https://github.com/spring-projects/spring-boot/tree/d0670d5b955cc54ff3ceb11dd3945bf4eab367e7/spring-boot-tools/spring-boot-configuration-metadata/src/main/java/org/springframework/boot/configurationmetadata

There are some changes made to the original code:

1. a hacked copy of org.json is used to preserve order of properties in json objects
2. ConfigurationMetadataRepositoryJsonBuilder has been fairly heavily modified to fix this bug:
   https://www.pivotaltracker.com/n/projects/1346850/stories/119220283
   Relevant changes: 
      https://github.com/spring-projects/spring-ide/commit/d927abedcd65f1078d52d311c81a1d5aa6da6bce
   The changes passing the 'origin' object can be ignored they only help debugging. (This way all other files besides
   the builder can be used without changes).