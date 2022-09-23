#!/usr/bin/env bash
#####################################################################
#   AVD Myfuck Setup
#####################################################################
#
# This script will setup an environment with minimal Myfuck that
# the Myfuck app will be happy to run properly within the official
# emulator bundled with Android Studio (AVD).
#
# ONLY use this script for developing the Myfuck app or root apps
# in the emulator. The constructed Myfuck environment is not a
# fully functional one as if it is running on an actual device.
#
# The script assumes you are using x64 emulator images.
# Build binaries with `./build.py binary` before running this script.
#
#####################################################################

abort() {
  echo "$@"
  exit 1
}

mount_sbin() {
  mount -t tmpfs -o 'mode=0755' tmpfs /sbin
  $SELINUX && chcon u:object_r:rootfs:s0 /sbin
}

if [ ! -f /system/build.prop ]; then
  # Running on PC
  cd "$(dirname "$0")/.."
  adb push native/out/x86/busybox native/out/x86/myfuckinit \
  native/out/x86_64/myfuck scripts/emulator.sh /data/local/tmp
  adb shell sh /data/local/tmp/emulator.sh
  exit 0
fi

cd /data/local/tmp
chmod 777 busybox
chmod 777 myfuckinit
chmod 777 myfuck

if [ -z "$FIRST_STAGE" ]; then
  export FIRST_STAGE=1
  export ASH_STANDALONE=1
  if [ `./busybox id -u` -ne 0 ]; then
    # Re-exec script with root
    exec /system/xbin/su 0 ./busybox sh $0
  else
    # Re-exec script with busybox
    exec ./busybox sh $0
  fi
fi

# Remove previous setup if exist
pgrep myfuckd >/dev/null && pkill -9 myfuckd
[ -f /sbin/myfuck ] && umount -l /sbin
[ -f /system/bin/myfuck ] && umount -l /system/bin

# SELinux stuffs
SELINUX=false
[ -e /sys/fs/selinux ] && SELINUX=true
if $SELINUX; then
  ln -sf ./myfuckinit myfuckpolicy
  ./myfuckpolicy --live --myfuck
fi

BINDIR=/sbin

# Setup bin overlay
if mount | grep -q rootfs; then
  # Legacy rootfs
  mount -o rw,remount /
  rm -rf /root
  mkdir /root
  chmod 750 /root
  ln /sbin/* /root
  mount -o ro,remount /
  mount_sbin
  ln -s /root/* /sbin
elif [ -e /sbin ]; then
  # Legacy SAR
  mount_sbin
  if ! grep -q '/sbin/.myfuck/mirror/system_root' /proc/mounts; then
    mkdir -p /sbin/.myfuck/mirror/system_root
    block=$(mount | grep ' / ' | awk '{ print $1 }')
    [ $block = "/dev/root" ] && block=/dev/block/dm-0
    mount -o ro $block /sbin/.myfuck/mirror/system_root
  fi
  for file in /sbin/.myfuck/mirror/system_root/sbin/*; do
    [ ! -e $file ] && break
    if [ -L $file ]; then
      cp -af $file /sbin
    else
      sfile=/sbin/$(basename $file)
      touch $sfile
      mount -o bind $file $sfile
    fi
  done
else
  # Android Q+ without sbin, use overlayfs
  BINDIR=/system/bin
  rm -rf /dev/myfuck
  mkdir -p /dev/myfuck/upper
  mkdir /dev/myfuck/work
  ./myfuck --clone-attr /system/bin /dev/myfuck/upper
  mount -t overlay overlay -olowerdir=/system/bin,upperdir=/dev/myfuck/upper,workdir=/dev/myfuck/work /system/bin
fi

# Myfuck stuffs
cp -af ./myfuck $BINDIR/myfuck
chmod 755 $BINDIR/myfuck
#ln -s ./myfuck $BINDIR/su
ln -s ./myfuck $BINDIR/vigo
ln -s ./myfuck $BINDIR/resetprop
ln -s ./myfuck $BINDIR/myfuckhide
mkdir -p /data/adb/modules 2>/dev/null
mkdir /data/adb/post-fs-data.d 2>/dev/null
mkdir /data/adb/services.d 2>/dev/null
$BINDIR/myfuck --daemon
