#pragma once

/* Include this header anywhere accessing MYFUCK_DEBUG, MYFUCK_VERSION, MYFUCK_VER_CODE.
 *
 * This file is for precise incremental builds. We can make sure code that uses
 * external flags are re-compiled by updating the timestamp of this file
 * */

#define quote(s) #s
#define str(s) quote(s)

#define MYFUCK_VERSION  str(__MVSTR)
#define MYFUCK_VER_CODE __MCODE
#define MYFUCK_FULL_VER MYFUCK_VERSION "(" str(MYFUCK_VER_CODE) ")"

#define NAME_WITH_VER(name) str(name) " " MYFUCK_FULL_VER

#ifdef __MDBG
#define MYFUCK_DEBUG
#endif
