#!/sbin/sh

#################
# Initialization
#################

umask 022

# echo before loading util_functions
ui_print() { echo "$1"; }

require_new_myfuck() {
  ui_print "*******************************"
  ui_print " Please install Myfuck v20.4+! "
  ui_print "*******************************"
  exit 1
}

#########################
# Load util_functions.sh
#########################

OUTFD=$2
ZIPFILE=$3

mount /data 2>/dev/null

[ -f /data/adb/myfuck/util_functions.sh ] || require_new_myfuck
. /data/adb/myfuck/util_functions.sh
[ $MAGISK_VER_CODE -lt 20400 ] && require_new_myfuck

install_module
exit 0
