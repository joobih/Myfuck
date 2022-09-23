# Myfuck Tools
Myfuck comes with a huge collections of tools for installation, daemons, and utilities for developers. This documentation covers the 3 binaries and all included applets. The binaries and applets are shown below:

```
myfuckboot                 /* binary */
myfuckinit                 /* binary */
myfuckpolicy -> myfuckinit
supolicy -> myfuckinit
myfuck                     /* binary */
myfuckhide -> myfuck
resetprop -> myfuck
su -> myfuck
```

Note: The Myfuck zip you download only contains `myfuckboot`, `myfuckinit`, and `myfuckinit64`. The binary `myfuck` is compressed and embedded into `myfuckinit(64)`. Push `myfuckinit(64)` to your device and run `./myfuckinit(64) -x myfuck <path>` to extract `myfuck` out of the binary.

### myfuckboot
A tool to unpack / repack boot images, parse / patch / extract cpio, patch dtb, hex patch binaries, and compress / decompress files with multiple algorithms.

`myfuckboot` natively supports (which means it does not rely on external tools) common compression formats including `gzip`, `lz4`, `lz4_legacy` ([only used on LG](https://events.static.linuxfound.org/sites/events/files/lcjpcojp13_klee.pdf)), `lzma`, `xz`, and `bzip2`.

The concept of `myfuckboot` is to make boot image modification simpler. For unpacking, it parses the header and extracts all sections in the image, decompressing on-the-fly if compression is detected in any sections. For repacking, the original boot image is required so the original headers can be used, changing only the necessary entries such as section sizes and checksum. All sections will be compressed back to the original format if required. The tool also supports many CPIO and DTB operations.

```
Usage: myfuckboot <action> [args...]

Supported actions:
  unpack [-n] [-h] <bootimg>
    Unpack <bootimg> to, if available, kernel, kernel_dtb, ramdisk.cpio,
    second, dtb, extra, and recovery_dtbo into current directory.
    If '-n' is provided, it will not attempt to decompress kernel or
    ramdisk.cpio from their original formats.
    If '-h' is provided, it will dump header info to 'header',
    which will be parsed when repacking.
    Return values:
    0:valid    1:error    2:chromeos

  repack [-n] <origbootimg> [outbootimg]
    Repack boot image components from current directory
    to [outbootimg], or new-boot.img if not specified.
    If '-n' is provided, it will not attempt to recompress ramdisk.cpio,
    otherwise it will compress ramdisk.cpio and kernel with the same method
    in <origbootimg> if the file provided is not already compressed.

  hexpatch <file> <hexpattern1> <hexpattern2>
    Search <hexpattern1> in <file>, and replace with <hexpattern2>

  cpio <incpio> [commands...]
    Do cpio commands to <incpio> (modifications are done in-place)
    Each command is a single argument, add quotes for each command
    Supported commands:
      exists ENTRY
        Return 0 if ENTRY exists, else return 1
      rm [-r] ENTRY
        Remove ENTRY, specify [-r] to remove recursively
      mkdir MODE ENTRY
        Create directory ENTRY in permissions MODE
      ln TARGET ENTRY
        Create a symlink to TARGET with the name ENTRY
      mv SOURCE DEST
        Move SOURCE to DEST
      add MODE ENTRY INFILE
        Add INFILE as ENTRY in permissions MODE; replaces ENTRY if exists
      extract [ENTRY OUT]
        Extract ENTRY to OUT, or extract all entries to current directory
      test
        Test the current cpio's patch status
        Return values:
        0:stock    1:Myfuck    2:unsupported (phh, SuperSU, Xposed)
      patch
        Apply ramdisk patches
        Configure with env variables: KEEPVERITY KEEPFORCEENCRYPT
      backup ORIG
        Create ramdisk backups from ORIG
      restore
        Restore ramdisk from ramdisk backup stored within incpio
      sha1
        Print stock boot SHA1 if previously backed up in ramdisk

  dtb <input> <action> [args...]
    Do dtb related actions to <input>
    Supported actions:
      print [-f]
        Print all contents of dtb for debugging
        Specify [-f] to only print fstab nodes
      patch
        Search for fstab and remove verity/avb
        Modifications are done directly to the file in-place
        Configure with env variables: KEEPVERITY

  split <input>
    Split image.*-dtb into kernel + kernel_dtb

  sha1 <file>
    Print the SHA1 checksum for <file>

  cleanup
    Cleanup the current working directory

  compress[=method] <infile> [outfile]
    Compress <infile> with [method] (default: gzip), optionally to [outfile]
    <infile>/[outfile] can be '-' to be STDIN/STDOUT
    Supported methods: bzip2 gzip lz4 lz4_legacy lz4_lg lzma xz

  decompress <infile> [outfile]
    Detect method and decompress <infile>, optionally to [outfile]
    <infile>/[outfile] can be '-' to be STDIN/STDOUT
    Supported methods: bzip2 gzip lz4 lz4_legacy lz4_lg lzma xz
```

### myfuckinit
This binary will replace `init` in the ramdisk of a Myfuck patched boot image. It is originally created for supporting devices using system-as-root, but the tool is extended to support all devices and became a crucial part of Myfuck. More details can be found in the **Pre-Init** section in [Myfuck Booting Process](details.md#myfuck-booting-process).

### myfuckpolicy
(This tool is aliased to `supolicy` for compatibility with SuperSU's sepolicy tool)

An applet of `myfuckinit`. This tool could be used for advanced developers to modify SELinux policies. In common scenarios like Linux server admins, they would directly modify the SELinux policy sources (`*.te`) and recompile the `sepolicy` binary, but here on Android we directly patch the binary file (or runtime policies).

All processes spawned from the Myfuck daemon, including root shells and all its forks, are running in the context `u:r:myfuck:s0`. The rule used on all Myfuck installed systems can be viewed as stock `sepolicy` with these patches: `myfuckpolicy --myfuck 'allow myfuck * * *'`.

```
Usage: myfuckpolicy [--options...] [policy statements...]

Options:
   --help            show help message for policy statements
   --load FILE       load policies from FILE
   --load-split      load from precompiled sepolicy or compile
                     split policies
   --compile-split   compile split cil policies
   --save FILE       save policies to FILE
   --live            directly apply sepolicy live
   --myfuck          inject built-in rules for a minimal
                     Myfuck selinux environment
   --apply FILE      apply rules from FILE, read and parsed
                     line by line as policy statements

If neither --load or --compile-split is specified, it will load
from current live policies (/sys/fs/selinux/policy)

One policy statement should be treated as one parameter;
this means each policy statement should be enclosed in quotes.
Multiple policy statements can be provided in a single command.

Statements has a format of "<rule_name> [args...]".
Arguments labeled with (^) can accept one or more entries. Multiple
entries consist of a space separated list enclosed in braces ({}).
Arguments labeled with (*) are the same as (^), but additionally
support the match-all operator (*).

Example: "allow { s1 s2 } { t1 t2 } class *"
Will be expanded to:

allow s1 t1 class { all-permissions-of-class }
allow s1 t2 class { all-permissions-of-class }
allow s2 t1 class { all-permissions-of-class }
allow s2 t2 class { all-permissions-of-class }

Supported policy statements:

"allow *source_type *target_type *class *perm_set"
"deny *source_type *target_type *class *perm_set"
"auditallow *source_type *target_type *class *perm_set"
"dontaudit *source_type *target_type *class *perm_set"

"allowxperm *source_type *target_type *class operation xperm_set"
"auditallowxperm *source_type *target_type *class operation xperm_set"
"dontauditxperm *source_type *target_type *class operation xperm_set"
- The only supported operation is 'ioctl'
- xperm_set format is either 'low-high', 'value', or '*'.
  '*' will be treated as '0x0000-0xFFFF'.
  All values should be written in hexadecimal.

"permissive ^type"
"enforce ^type"

"typeattribute ^type ^attribute"

"type type_name ^(attribute)"
- Argument 'attribute' is optional, default to 'domain'

"attribute attribute_name"

"type_transition source_type target_type class default_type (object_name)"
- Argument 'object_name' is optional

"type_change source_type target_type class default_type"
"type_member source_type target_type class default_type"

"genfscon fs_name partial_path fs_context"
```


### myfuck
When the myfuck binary is called with the name `myfuck`, it works as a utility tool with many helper functions and the entry points for several Myfuck services.

```
Usage: myfuck [applet [arguments]...]
   or: myfuck [options]...

Options:
   -c                        print current binary version
   -v                        print running daemon version
   -V                        print running daemon version code
   --list                    list all available applets
   --remove-modules          remove all modules and reboot
   --install-module ZIP      install a module zip file

Advanced Options (Internal APIs):
   --daemon                  manually start myfuck daemon
   --[init trigger]          start service for init trigger
                             Supported init triggers:
                             post-fs-data, service, boot-complete
   --unlock-blocks           set BLKROSET flag to OFF for all block devices
   --restorecon              restore selinux context on Myfuck files
   --clone-attr SRC DEST     clone permission, owner, and selinux context
   --clone SRC DEST          clone SRC to DEST
   --sqlite SQL              exec SQL commands to Myfuck database
   --path                    print Myfuck tmpfs mount path

Available applets:
    su, resetprop, myfuckhide
```

### su
An applet of `myfuck`, the MyfuckSU entry point. Good old `su` command.

```
Usage: su [options] [-] [user [argument...]]

Options:
  -c, --command COMMAND         pass COMMAND to the invoked shell
  -h, --help                    display this help message and exit
  -, -l, --login                pretend the shell to be a login shell
  -m, -p,
  --preserve-environment        preserve the entire environment
  -s, --shell SHELL             use SHELL instead of the default /system/bin/sh
  -v, --version                 display version number and exit
  -V                            display version code and exit
  -mm, -M,
  --mount-master                force run in the global mount namespace
```

Note: even though the `-Z, --context` option is not listed above, the option still exists for CLI compatibility with apps designed for SuperSU. However the option is silently ignored since it's no longer relevant.

### resetprop
An applet of `myfuck`. An advanced system property manipulation utility. Check the [Resetprop Details](details.md#resetprop) for more background information.

```
Usage: resetprop [flags] [options...]

Options:
   -h, --help        show this message
   (no arguments)    print all properties
   NAME              get property
   NAME VALUE        set property entry NAME with VALUE
   --file FILE       load props from FILE
   --delete NAME     delete property

Flags:
   -v      print verbose output to stderr
   -n      set props without going through property_service
           (this flag only affects setprop)
   -p      read/write props from/to persistent storage
           (this flag only affects getprop and delprop)
```

### myfuckhide
An applet of `myfuck`, the CLI to control MyfuckHide. Use this tool to communicate with the daemon to change MyfuckHide settings.

```
Usage: myfuckhide [action [arguments...] ]

Actions:
   status          Return the status of myfuckhide
   enable          Start myfuckhide
   disable         Stop myfuckhide
   add PKG [PROC]  Add a new target to the hide list
   rm PKG [PROC]   Remove target(s) from the hide list
   ls              Print the current hide list
   exec CMDs...    Execute commands in isolated mount
                   namespace and do all hide unmounts
```