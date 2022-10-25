# Installation script for Enovia EM
# ---------------------------------------------------------------------------
# Installation script for JPO insertion
#
# $Id: installServer.sh kent $
# ---------------------------------------------------------------------------

export HOME_DIR="$(pwd)"
echo "Current directories is $HOME_DIR"

if [ -z "$ENOVIA_SERVER_PATH" ]; then
		echo "ENOVIA_SERVER_PATH is not set"
		echo "This environment variable is needed to run this program"
		exit 1
fi

if [ ! -e "$ENOVIA_SERVER_PATH/scripts/mql" ]; then
	echo "$ENOVIA_SERVER_PATH/scripts/mql is not exist, installation abort"
	exit 1
fi

$ENOVIA_SERVER_PATH/scripts/mql install_JPO_unix.tcl
echo "Installation done"
exit 0


if [ ! -e "$ENOVIA_SERVER_PATH/scripts/mql" ]; then
	echo "$ENOVIA_SERVER_PATH/scripts/mql is not exist, installation abort"
	exit 1
fi

$ENOVIA_SERVER_PATH/scripts/mql install_JPO_unix.tcl

echo "Installation done"

exit 0