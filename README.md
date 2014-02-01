# Spring IDE

  Spring IDE enhances a regular Eclipse IDE with tooling for building Spring-based applications.
  It makes your Eclipse IDE Spring-aware and adds code-completion, validation, content-assist,
  and quick-fixes to all different kinds of Spring-related artifacts (Spring configuration files,
  Spring annotations, etc.). It also contains support for refactoring, Spring AOP and AspectJ,
  auto-wiring, Spring Batch, Spring Integration, Spring Security, and Spring Webflow, and is
  integrated with Eclipse Mylyn.

  It also contains Spring UAA, an optional component that helps us to collect some usage data. 
  This is completely anonymous and helps us to understand better how the tooling is used and how 
  to improve it in the future.

  It also comes with the SpringSource Dashboard is an optional component, which brings you
  up-to-date information about SpringSource-related projects as well as an easy-to-use extension
  install to get additional tooling add-ons, like the VMware vFabric tc Server Integration for
  Eclipse or the Cloud Foundry Integration for Eclipse.

## Installation (Release)

  First, you can install Spring IDE from the Eclipse Marketplace into your existing Eclipse installation.
  Second, you can install Spring IDE manually from this udpate site:

  http://dist.springsource.com/release/TOOLS/update/e4.2 (for Eclipse Juno 4.2)
  http://dist.springsource.com/release/TOOLS/update/e3.7 (for Eclipse Indigo 3.7)
  http://dist.springsource.com/release/TOOLS/update/e3.6 (for Eclipse Helios 3.6)

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

  http://www.springsource.com/webinar/getting-started-spring-and-springsource-tool-suite

## Questions and bug reports:

  If you have a question that Google can't answer, the best way is to go to the forum:

  http://forum.springsource.org/forumdisplay.php?32-SpringSource-Tool-Suite

  There you can also ask questions and search for other people with related or similar problems
  (and solutions). New versions of Spring IDE (and the SpringSource Tool Suite) are announced
  there as well.

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

  `mvn -Pe37 clean install`

## Contributing

  Here are some ways for you to get involved in the community:

  * Get involved with the Spring community on the Spring Community Forums.  Please help out on the [forum](http://forum.springsource.org/forumdisplay.php?32-SpringSource-Tool-Suite) by responding to questions and joining the debate.
  * Create [JIRA](https://jira.springsource.org/browse/IDE) tickets for bugs and new features and comment and vote on the ones that you are interested in.  
  * Github is for social coding: if you want to write code, we encourage contributions through pull requests from [forks of this repository](http://help.github.com/forking/). If you want to contribute code this way, please reference a JIRA ticket as well covering the specific issue you are addressing.
  * Watch for upcoming articles on Spring by [subscribing](http://www.springsource.org/node/feed) to springframework.org

Before we accept a non-trivial patch or pull request we will need you to sign the [contributor's agreement](https://support.springsource.com/spring_eclipsecla_committer_signup). Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, and you will get an author credit if we do. Active contributors might be asked to join the core team, and given the ability to merge pull requests.
