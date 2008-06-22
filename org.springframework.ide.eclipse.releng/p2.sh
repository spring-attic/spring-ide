#!/bin/bash
# create p2 metadata for a given update site

# REQUIREMENTS 
# vm (/path/to/java if not /usr/bin/java)
# targetDir (/path/to/output/the/site.xml file)
# basebuilderDir (/path/to/org.eclipse.releng.basebuilder)
# trackstats (true if we're generating site.xml w/ http:// urls for tracking download stats)
# projectNameVanity (name of the project, or default default: "Modeling")

norm="\033[0;39m";
grey="\033[1;30m";
green="\033[1;32m";
brown="\033[0;33m";
yellow="\033[1;33m";
blue="\033[1;34m";
cyan="\033[1;36m";
red="\033[1;31m";

vm=/usr/bin/java
debug=0;
trackstats=false;
projectNameVanity="";

##########################################################################################

function usage ()
{
	echo " "
	echo "usage: ${0##*/}"
	echo "-targetDir /path/to/\$label/             (required: path to dir containing site.xml)"
	echo "-basebuilderDir /path/to/org.eclipse.releng.basebuilder (optional: will try to guess)"
	echo "-debug [1|2]                         	(optional)"
	echo "-vm /path/to/java                    	(optional; default: $vm)" 
	echo "-trackstats                             (optional; default: false)"
	echo "-p, -projectNameVanity projectName      (optional; default: \"Modeling\")"
	echo "examples:"
	echo "	${0##*/} -p EMF -basebuilderDir /home/www-data/build/org.eclipse.releng.basebuilder -targetDir \$localUpdatesWebDir"
	echo "	${0##*/} -p GMF -basebuilderDir /opt/public/modeling/build/org.eclipse.releng.basebuilder -targetDir \$updatesDir"
	exit 1;
}

if [ $# -lt 2 ]; then
	usage;
fi

while [ "$#" -gt 0 ]; do
	case $1 in
		'-debug')		debug=$2;				shift 2;;
		'-vm')			vm=$2;					shift 2;;
		'-targetDir')	targetDir=$2;			shift 2;;
		'-basebuilderDir') basebuilderDir=$2;	shift 2;;
		'-trackstats')	trackstats=$2;			shift 2;;
		'-p'|'-projectNameVanity')	projectNameVanity=$2; shift 2;;
		*) projectNameVanity=$projectNameVanity" "$1; shift 1;;
	esac
done

if [[ ! $projectNameVanity ]]; then projectNameVanity="Modeling"; fi

if [[ ! $basebuilderDir ]] || [[ ! -d $basebuilderDir ]]; then
	for d in /home/www-data/build/org.eclipse.releng.basebuilder /opt/public/modeling/build/org.eclipse.releng.basebuilder; do
		if [[ -d $d ]]; then
			basebuilderDir=$d;
			break;
		fi
	done
fi
if [[ ! $basebuilderDir ]] || [[ ! -d $basebuilderDir ]]; then
	usage;
fi

##########################################################################################

# create site metadata for the composite site
echo -e -n '[  p2  ] ['`date +%H:%M:%S`'] Generate '$blue$targetDir$norm/${blue}artifacts.jar${norm} and ${blue}content.jar${norm}' ... '
if [[ $trackstats == "true" ]]; then
	# temporarily remove any download tracking urls in <feature>s
	pushd ${targetDir} >/dev/null;
	mv -f site.xml site-full.xml;
	cat site-full.xml | \
		perl -pe "s#http://www.eclipse.org/downloads/download.php\?r=1\&amp;file=.+features/#features/#g" \
		> site.xml;
fi
pushd $basebuilderDir >/dev/null;
generatorJar=$(find $basebuilderDir/plugins -name "org.eclipse.equinox.p2.metadata.generator_*.jar" | head -1);
launcherJar=$(find $basebuilderDir/plugins -name "org.eclipse.equinox.launcher_*.jar" -o -name "org.eclipse.equinox.launcher.jar" | head -1);

if [[ $debug -gt 0 ]]; then 
	$vm -cp $launcherJar:$generatorJar:$basebuilderDir/plugins org.eclipse.equinox.launcher.Main \
	  -application org.eclipse.equinox.p2.metadata.generator.EclipseGenerator \
	  -updateSite ${targetDir}/ -site file:${targetDir}/site.xml \
	  -metadataRepository file:${targetDir}/ -metadataRepositoryName "$projectNameVanity Update Site" \
	  -artifactRepository file:${targetDir}/ -artifactRepositoryName "$projectNameVanity Artifacts" \
	  -compress -reusePack200Files -vmargs -Xmx128M
else
	$vm -cp $launcherJar:$generatorJar:$basebuilderDir/plugins org.eclipse.equinox.launcher.Main \
	  -application org.eclipse.equinox.p2.metadata.generator.EclipseGenerator \
	  -updateSite ${targetDir}/ -site file:${targetDir}/site.xml \
	  -metadataRepository file:${targetDir}/ -metadataRepositoryName "$projectNameVanity Update Site" \
	  -artifactRepository file:${targetDir}/ -artifactRepositoryName "$projectNameVanity Artifacts" \
	  -compress -reusePack200Files -vmargs -Xmx128M 2>&1 >/dev/null;
fi

popd >/dev/null;

if [[ $trackstats == "true" ]]; then
	# undo temporary changes to site.xml
	mv -f site-full.xml site.xml;
	popd >/dev/null;
fi
	
if [[ ! -f $targetDir/artifacts.jar ]] || [[ ! -f $targetDir/content.jar ]]; then
	if [[ $debug -eq 0 ]]; then echo ""; fi # line break
	echo -e '[  p2  ] ['`date +%H:%M:%S`'] '"${red}*!* Warning: metadata not created. ${red}*!*${norm}";
else
	if [[ $debug -gt 0 ]]; then 
		echo '[  p2  ] ['`date +%H:%M:%S`'] Done.';
	else
		echo '['`date +%H:%M:%S`']';
	fi
fi


# ./eclipse -application org.eclipse.equinox.p2.metadata.generator.EclipseGenerator -updateSite /Users/cdupuis/Development/Java/work/spring-ide/trunk/org.springframework.ide.eclipse.releng/updatesite/ -site file:///Users/cdupuis/Development/Java/work/spring-ide/trunk/org.springframework.ide.eclipse.releng/updatesite/site.xml -metadataRepository file:///Users/cdupuis/Development/Java/work/spring-ide/trunk/org.springframework.ide.eclipse.releng/updatesite/ -metadataRepositoryName "Ganymede Update Site" -artifactRepository file:///Users/cdupuis/Development/Java/work/spring-ide/trunk/org.springframework.ide.eclipse.releng/updatesite/ -artifactRepositoryName "Ganymede Artifacts" -compress -reusePack200Files -noDefaultIUs -vmargs -Xmx256m