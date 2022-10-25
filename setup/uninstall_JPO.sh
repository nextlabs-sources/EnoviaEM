# Installation script for Enovia EM
# ---------------------------------------------------------------------------
# Uninstallation script for JPO insertion
#
# $Id: uninstall_JPO.sh kent $
# ---------------------------------------------------------------------------

unistallJPO() {

	CURRENT_DIR="$(pwd)"
	echo "Current directories is $CURRENT_DIR"

	if [ -z "$ENOVIA_SERVER_PATH" ]; then
		echo "ENOVIA_SERVER_PATH is not set"
		echo "This environment variable is needed to run this program"
		exit 1
	fi

	if [ ! -e "$ENOVIA_SERVER_PATH/scripts/mql" ]; then
		echo "$ENOVIA_SERVER_PATH/scripts/mql is not exist, uninstallation abort"
		exit 1
	fi

	$ENOVIA_SERVER_PATH/scripts/mql uninstall_JPO_unix.tcl
	echo "JPO remove successfully"
	exit 0
}


while true; do
    read -p "Uninstalling JPO for Enovia EM? (y/n)" yn
    case $yn in
        [Yy]* ) unistallJPO; break;;
        [Nn]* ) exit;;
        * ) echo "Please answer y or n.";;
    esac
done

