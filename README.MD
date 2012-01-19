# Spring IDE
      
  Spring IDE enhances a regular Eclipse IDE with tooling for building Spring-based applications.
  It makes your Eclipse IDE Spring-aware and adds code-completion, validation, content-assist,
  and quick-fixes to all different kinds of Spring-related artifacts (Spring configuration files,
  Spring annotations, etc.). It also contains support for refactoring, Spring AOP and AspectJ,
  auto-wiring, Spring Batch, Spring Integration, Spring Security, and Spring Webflow, and is
  integrated with Eclipse Mylyn.

  It also contains Spring UAA (User Agent Analysis), an optional component that help us to
  collect some usage data. This is completely anonymous and helps us to understand better how
  the tooling is used and how to improve it in the future.

## Installation

  First, you can install Spring IDE from the Eclipse Marketplace into your existing Eclipse installation.
  Second, you can install Spring IDE manually from this udpate site:

  http://dist.springsource.com/release/TOOLS/update/e3.7 (for Eclipse Indigo 3.7)
  http://dist.springsource.com/release/TOOLS/update/e3.6 (for Eclipse Helios 3.6)

## Getting started

  There is a webinar online that is focused introductory material for Spring itself and the
  SpringSource Tool Suite, which contains Spring IDE, and is therefore a good point to start
  if you never used Spring tooling before:

  http://www.springsource.com/webinar/getting-started-spring-and-springsource-tool-suite

## Questions and bug reports:

  If you have a question that Google can't answer, the best way is to go to the forum:

  http://forum.springsource.org/forumdisplay.php?32-SpringSource-Tool-Suite

  There you can also ask questions and search for other people with related or similar problems
  (and solutions). New versions of Spring IDE (and the SpringSource Tool Suite) are announced
  there as well.

  With regards to bug reports, please go to:

  https://jira.springsource.org/browse/IDE


## Contributing to Spring IDE

  If you wanna work on the project itself, the best way is to install Spring IDE into your Eclipse
  target platform and start from there, using the standard Eclipse way of plugin development using PDE.
  You can clone the Spring IDE git repository and import the projects into your Eclipse workspace
  and start using them.

## Building Spring IDE
  
  The Spring IDE project uses Maven Tycho to do continuous integration builds and to produce
  p2 repos and update sites. To build Spring IDE itself, you can execute:

  mvn -Dp2.qualifier=SNAPSHOT -Pe36 clean install

