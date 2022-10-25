# Installation script for Enovia EM
# ---------------------------------------------------------------------------
# Installation script
#
# $Id: install_Studio.sh kent $
# ---------------------------------------------------------------------------
# STUDIO_CUSTOM_APP_HOME= /opt/pdm/2016x/studio/linux_a64/docs/custom [sample location]

if [ -z "$STUDIO_CUSTOM_APP_HOME" ]; then
    echo "STUDIO_CUSTOM_APP_HOME is not set, please set the STUDIO_CUSTOM_APP_HOME"
	echo "This environment variable is needed to run this program"
    exit 1
fi

if [ ! -d "$STUDIO_CUSTOM_APP_HOME" ]; then
	echo ""$STUDIO_CUSTOM_APP_HOME" is not exists in the system, installation abort"
fi

mkdir $STUDIO_CUSTOM_APP_HOME/nextlabs
mkdir $STUDIO_CUSTOM_APP_HOME/nextlabs/conf
mkdir $STUDIO_CUSTOM_APP_HOME/nextlabs/logs
echo "Directories created successfully"

cp ../nextlabs-enovia-em.jar $STUDIO_CUSTOM_APP_HOME/
cp ../xlib/*.* $STUDIO_CUSTOM_APP_HOME/
cp ../conf/*.* $STUDIO_CUSTOM_APP_HOME/nextlabs/conf
echo "Files copied successfully"

echo "Installation done"