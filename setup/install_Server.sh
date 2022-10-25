# Installation script for Enovia EM
# ---------------------------------------------------------------------------
# Installation script
#
# $Id: install_ELCS.sh kent $
# ---------------------------------------------------------------------------
# DSPACE_CUSTOM_APP_HOME = /opt/pdm/2016x/3dspace/linux_a64/docs/custom [sample location]

if [ -z "$DSPACE_CUSTOM_APP_HOME" ]; then
    echo "DSPACE_CUSTOM_APP_HOME is not set, please set the DSPACE_CUSTOM_APP_HOME"
	echo "This environment variable is needed to run this program"
    exit 1
fi

if [ ! -d "$DSPACE_CUSTOM_APP_HOME" ]; then
	echo ""$DSPACE_CUSTOM_APP_HOME" is not exists in the system, installation abort"
fi

mkdir $DSPACE_CUSTOM_APP_HOME/nextlabs
mkdir $DSPACE_CUSTOM_APP_HOME/nextlabs/conf
mkdir $DSPACE_CUSTOM_APP_HOME/nextlabs/logs
echo "Directories created successfully"

cp ../nextlabs-enovia-em.jar $DSPACE_CUSTOM_APP_HOME/
cp ../xlib/*.* $DSPACE_CUSTOM_APP_HOME/
cp ../conf/*.* $DSPACE_CUSTOM_APP_HOME/nextlabs/conf
echo "Files copied successfully"

echo "Installation done"