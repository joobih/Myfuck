#include <myfuck.hpp>
#include <selinux.hpp>

#define quote(s) #s
#define str(s) quote(s)

constexpr char MYFUCK_RC[] =
"\n"

"on post-fs-data\n"
"    start logd\n"
"    rm " UNBLOCKFILE "\n"
"    start %2$s\n"
"    wait " UNBLOCKFILE " " str(POST_FS_DATA_WAIT_TIME) "\n"
"    rm " UNBLOCKFILE "\n"
"\n"

"service %2$s %1$s/myfuck --post-fs-data\n"
"    user root\n"
"    seclabel u:r:" SEPOL_PROC_DOMAIN ":s0\n"
"    oneshot\n"
"\n"

"service %3$s %1$s/myfuck --service\n"
"    class late_start\n"
"    user root\n"
"    seclabel u:r:" SEPOL_PROC_DOMAIN ":s0\n"
"    oneshot\n"
"\n"

"on property:sys.boot_completed=1\n"
"    exec %1$s/myfuck --boot-complete\n"
"\n"

"on property:init.svc.zygote=restarting\n"
"    exec %1$s/myfuck --zygote-restart\n"
"\n"

"on property:init.svc.zygote=stopped\n"
"    exec %1$s/myfuck --zygote-restart\n"
"\n"
;
