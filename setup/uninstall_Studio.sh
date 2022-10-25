# Unistallation script for Enovia EM
# ---------------------------------------------------------------------------
# Unistallation script script
#
# $Id: unistall_ELCS.sh kent $
# ---------------------------------------------------------------------------

uninstallEnovia() {
	if [ -z "$STUDIO_CUSTOM_APP_HOME" ]; then
    	echo "STUDIO_CUSTOM_APP_HOME is not set, please set the STUDIO_CUSTOM_APP_HOME"
		echo "This environment variable is needed to run this program"
    	exit 1
	fi

	if [ ! -d "$STUDIO_CUSTOM_APP_HOME" ]; then
		echo ""$STUDIO_CUSTOM_APP_HOME" is not exists in the system, uninstallation abort"
	fi

	rmdir -r $STUDIO_CUSTOM_APP_HOME/nextlabs
	rm $STUDIO_CUSTOM_APP_HOME/commons-lang3-3.3.2.jar
	rm $STUDIO_CUSTOM_APP_HOME/commons-configuration-1.8.jar
	rm $STUDIO_CUSTOM_APP_HOME/commons-logging-1.1.1.jar
	rm $STUDIO_CUSTOM_APP_HOME/ehcache-2.10.3.jar
	rm $STUDIO_CUSTOM_APP_HOME/log4j-1.2.17.jar
	rm $STUDIO_CUSTOM_APP_HOME/slf4j-api-1.7.21.jar
	rm $STUDIO_CUSTOM_APP_HOME/slf4j-ext-1.7.21.jar
	rm $STUDIO_CUSTOM_APP_HOME/nextlabs-openaz-pep.jar
	rm $STUDIO_CUSTOM_APP_HOME/openaz-pep-0.0.1-SNAPSHOT.jar
	rm $STUDIO_CUSTOM_APP_HOME/openaz-xacml-0.0.1-SNAPSHOT.jar
	rm $STUDIO_CUSTOM_APP_HOME/jackson-databind-2.6.3.jar
	rm $STUDIO_CUSTOM_APP_HOME/jackson-core-2.6.3.jar
	rm $STUDIO_CUSTOM_APP_HOME/jackson-annotations-2.6.0.jar
	rm $STUDIO_CUSTOM_APP_HOME/httpcore-4.3.jar
	rm $STUDIO_CUSTOM_APP_HOME/httpclient-4.3.1.jar
	rm $STUDIO_CUSTOM_APP_HOME/guava-19.0.jar
	rm $STUDIO_CUSTOM_APP_HOME/commons-lang-2.6.jar
	rm $STUDIO_CUSTOM_APP_HOME/crypt.jar
	rm $STUDIO_CUSTOM_APP_HOME/nextlabs-enovia-em.jar
	echo "Uninstallation done!"
	
	}

while true; do
    read -p "Uninstalling Enovia EM? (y/n)" yn

    case $yn in
        [Yy]* ) uninstallEnovia; break;;
        [Nn]* ) exit;;
        * ) echo "Please answer y or n.";;
    esac
done