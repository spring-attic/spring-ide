# Spring IDE 

  Spring IDE enhances a regular Eclipse IDE with tooling for building Spring-based applications.
  It makes your Eclipse IDE Spring-aware and adds code-completion, validation, content-assist,
  and quick-fixes to all different kinds of Spring-related artifacts (Spring configuration files,
  Spring annotations, etc.). It also contains support for refactoring, Spring AOP and AspectJ,
  auto-wiring, Spring Batch, Spring Integration, Spring Security, and Spring Webflow, and is
  integrated with Eclipse Mylyn.

  It also comes with the Spring Dashboard is an optional component, which brings you
  up-to-date information about SpringSource-related projects as well as an easy-to-use extension
  install to get additional tooling add-ons, like the Pivotal tc Server Integration for
  Eclipse or the Cloud Foundry Integration for Eclipse.

## Installation (Release)

  First, you can install Spring IDE from the Eclipse Marketplace into your existing Eclipse installation.
  Second, you can install Spring IDE manually from this udpate site:

  https://dist.springsource.com/release/TOOLS/update/e4.12 (for Eclipse 2019-06 4.12)
  https://dist.springsource.com/release/TOOLS/update/e4.11 (for Eclipse 2019-03 4.11)
  https://dist.springsource.com/release/TOOLS/update/e4.10 (for Eclipse 2018-12 4.10)
  https://dist.springsource.com/release/TOOLS/update/e4.9 (for Eclipse 2018-09 4.9)
  
  The latest versions of Spring IDE support Eclipse 2018-09, 2018-12, 2019-03, and 2019-06. Older versions of Spring IDE
  for older versions of Eclipse can be found here:

  https://dist.springsource.com/release/TOOLS/update/e4.8 (for Eclipse Photon 4.8)
  https://dist.springsource.com/release/TOOLS/update/e4.7 (for Eclipse Oxygen 4.7)
  https://dist.springsource.com/release/TOOLS/update/e4.6 (for Eclipse 4.6)
  https://dist.springsource.com/release/TOOLS/update/e4.5 (for Eclipse 4.5)
  https://dist.springsource.com/release/TOOLS/update/e4.4 (for Eclipse 4.4)
  https://dist.springsource.com/release/TOOLS/update/e4.3 (for Eclipse 4.3)
  https://dist.springsource.com/release/TOOLS/update/e3.8 (for Eclipse 3.8)
  https://dist.springsource.com/release/TOOLS/update/e3.7 (for Eclipse 3.7)
  
  However, we do not support those older versions of Spring IDE anymore.

## Installation (Milestone)

  You can install the latest milestone build of the Spring IDE manually from this udpate site:

  https://dist.springframework.org/milestone/IDE/

## Installation (CI builds)

  If you want to live on the bleading egde, you can also install always up-to-date continuous
  integration buids from this update site:

  https://dist.springframework.org/snapshot/IDE/nightly/

  But take care, those builds could be broken from time to time and might contain non-ship-ready
  features that might never appear in the milestone or release builds.

## Getting started

  There is a webinar online that is focused introductory material for Spring itself and the
  Spring Tool Suite, which contains Spring IDE, and is therefore a good point to start
  if you never used Spring tooling before:

  https://www.youtube.com/playlist?list=PL7B74449D5224CC99

## Questions and bug reports:

  If you have a question that Google can't answer, the best way is to go to the stackoverflow
  using the tag `spring-tool-suite`:

  https://stackoverflow.com/tags/spring-tool-suite[`spring-tool-suite`]
  
  Bug reports and enhancement requests are tracked using GitHub issues here:
  
  https://github.com/spring-projects/spring-ide/issues

## Developing Spring IDE

  If you wanna work on the project itself, the best way is to install Spring IDE into your Eclipse
  target platform and start from there, using the standard Eclipse way of plugin development using PDE.
  You can clone the Spring IDE git repository and import the projects into your Eclipse workspace
  and start using them. 

## Building Spring IDE
  
  The Spring IDE project uses Maven Tycho to do continuous integration builds and to produce
  p2 repos and update sites. To build Spring IDE itself, you can execute:

  `mvn -Pe47 clean install`
