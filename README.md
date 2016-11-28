# Spring IDE 

  Spring IDE enhances a regular Eclipse IDE with tooling for building Spring-based applications.
  It makes your Eclipse IDE Spring-aware and adds code-completion, validation, content-assist,
  and quick-fixes to all different kinds of Spring-related artifacts (Spring configuration files,
  Spring annotations, etc.). It also contains support for refactoring, Spring AOP and AspectJ,
  auto-wiring, Spring Batch, Spring Integration, Spring Security, and Spring Webflow, and is
  integrated with Eclipse Mylyn.

  It also comes with the SpringSource Dashboard is an optional component, which brings you
  up-to-date information about SpringSource-related projects as well as an easy-to-use extension
  install to get additional tooling add-ons, like the Pivotal tc Server Integration for
  Eclipse or the Cloud Foundry Integration for Eclipse.

## Installation (Release)

  First, you can install Spring IDE from the Eclipse Marketplace into your existing Eclipse installation.
  Second, you can install Spring IDE manually from this udpate site:

  http://dist.springsource.com/release/TOOLS/update/e4.6 (for Eclipse Neon 4.6)
  http://dist.springsource.com/release/TOOLS/update/e4.5 (for Eclipse Mars 4.5)
  
  The latest versions of Spring IDE support Eclipse Mars and Neon. Older versions of Spring IDE
  for older versions of Eclipse can be found here:
  
  http://dist.springsource.com/release/TOOLS/update/e4.3 (for Eclipse 4.3)
  http://dist.springsource.com/release/TOOLS/update/e3.8 (for Eclipse 3.8)
  http://dist.springsource.com/release/TOOLS/update/e3.7 (for Eclipse 3.7)
  
  However, we do not support those older versions of Spring IDE anymore.

## Installation (Milestone)

  You can install the latest milestone build of the Spring IDE manually from this udpate site:

  https://dist.springframework.org/milestone/IDE/

## Installation (CI builds)

  If you want to live on the bleading egde, you can also install always up-to-date continuous
  integration buids from this update site:

  http://dist.springframework.org/snapshot/IDE/nightly/

  But take care, those builds could be broken from time to time and might contain non-ship-ready
  features that might never appear in the milestone or release builds.

## Getting started

  There is a webinar online that is focused introductory material for Spring itself and the
  SpringSource Tool Suite, which contains Spring IDE, and is therefore a good point to start
  if you never used Spring tooling before:

  https://www.youtube.com/playlist?list=PL7B74449D5224CC99

## Questions and bug reports:

  If you have a question that Google can't answer, the best way is to go to Stackoverflow:

  http://stackoverflow.com/questions/tagged/spring-tool-suite

  Tag your question with "spring-tool-suite" and we will come across it.

  With regards to bug reports, please go to:

  https://jira.springsource.org/browse/IDE

## Developing Spring IDE

  If you wanna work on the project itself, the best way is to install Spring IDE into your Eclipse
  target platform and start from there, using the standard Eclipse way of plugin development using PDE.
  You can clone the Spring IDE git repository and import the projects into your Eclipse workspace
  and start using them. 

## Building Spring IDE
  
  The Spring IDE project uses Maven Tycho to do continuous integration builds and to produce
  p2 repos and update sites. To build Spring IDE itself, you can execute:

  `mvn -Pe45 clean install`

## Contributing

### Code of Conduct
This project adheres to the Contributor Covenant [code of
conduct](CODE_OF_CONDUCT.adoc). By participating, you  are expected to uphold this code. Please report
unacceptable behavior to spring-code-of-conduct@pivotal.io.

### Get Involved
  Here are some ways for you to get involved in the community:

  * Get involved with the Spring community on Stackoverflow.  Please help out there and answer questions or join the debate: http://stackoverflow.com/questions/tagged/spring-tool-suite.
  * Create [JIRA](https://jira.springsource.org/browse/IDE) tickets for bugs and new features and comment and vote on the ones that you are interested in.  
  * Github is for social coding: if you want to write code, we encourage contributions through pull requests from [forks of this repository](http://help.github.com/forking/). If you want to contribute code this way, please reference a JIRA ticket as well covering the specific issue you are addressing.
  * Watch for upcoming articles on Spring by [subscribing](https://spring.io/blog) to https://spring.io/blog.

### Contributor License Agreement
Before we accept a non-trivial patch or pull request we will need you to sign the [contributor's agreement](https://support.springsource.com/spring_eclipsecla_committer_signup). Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, and you will get an author credit if we do. Active contributors might be asked to join the core team, and given the ability to merge pull requests.
