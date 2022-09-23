#include <utils.hpp>
#include <cpio.hpp>

#include "myfuckboot.hpp"
#include "compress.hpp"

using namespace std;

constexpr char RAMDISK_XZ[] = "ramdisk.cpio.xz";

static const char *UNSUPPORT_LIST[] =
        { "sbin/launch_daemonsu.sh", "sbin/vigo", "init.xposed.rc",
          "boot/sbin/launch_daemonsu.sh" };

static const char *MAGISK_LIST[] =
        { ".backup/.myfuck", "init.myfuck.rc",
          "overlay/init.myfuck.rc" };

class myfuck_cpio : public cpio_rw {
public:
    myfuck_cpio() = default;
    explicit myfuck_cpio(const char *filename) : cpio_rw(filename) {}
    void patch();
    int test();
    char *sha1();
    void restore();
    void backup(const char *orig);
    void compress();
    void decompress();
};

bool check_env(const char *name) {
    const char *val = getenv(name);
    return val != nullptr && val == "true"sv;
}

void myfuck_cpio::patch() {
    bool keepverity = check_env("KEEPVERITY");
    bool keepforceencrypt = check_env("KEEPFORCEENCRYPT");
    fprintf(stderr, "Patch with flag KEEPVERITY=[%s] KEEPFORCEENCRYPT=[%s]\n",
            keepverity ? "true" : "false", keepforceencrypt ? "true" : "false");

    for (auto it = entries.begin(); it != entries.end();) {
        auto cur = it++;
        bool fstab = (!keepverity || !keepforceencrypt) &&
                     S_ISREG(cur->second->mode) &&
                     !str_starts(cur->first, ".backup") &&
                     !str_contains(cur->first, "twrp") &&
                     !str_contains(cur->first, "recovery") &&
                     str_contains(cur->first, "fstab");
        if (!keepverity) {
            if (fstab) {
                fprintf(stderr, "Found fstab file [%s]\n", cur->first.data());
                cur->second->filesize = patch_verity(cur->second->data, cur->second->filesize);
            } else if (cur->first == "verity_key") {
                rm(cur);
                continue;
            }
        }
        if (!keepforceencrypt) {
            if (fstab) {
                cur->second->filesize = patch_encryption(cur->second->data, cur->second->filesize);
            }
        }
    }
}

#define STOCK_BOOT        0
#define MAGISK_PATCHED    (1 << 0)
#define UNSUPPORTED_CPIO  (1 << 1)
#define COMPRESSED_CPIO   (1 << 2)

int myfuck_cpio::test() {
    for (auto file : UNSUPPORT_LIST)
        if (exists(file))
            return UNSUPPORTED_CPIO;

    int flags = STOCK_BOOT;

    if (exists(RAMDISK_XZ)) {
        flags |= COMPRESSED_CPIO | MAGISK_PATCHED;
        decompress();
    }

    for (auto file : MAGISK_LIST) {
        if (exists(file)) {
            flags |= MAGISK_PATCHED;
            break;
        }
    }

    return flags;
}

#define for_each_line(line, buf, size) \
for (line = (char *) buf; line < (char *) buf + size && line[0]; line = strchr(line + 1, '\n') + 1)

char *myfuck_cpio::sha1() {
    decompress();
    char sha1[41];
    char *line;
    for (auto &e : entries) {
        if (e.first == "init.myfuck.rc" || e.first == "overlay/init.myfuck.rc") {
            for_each_line(line, e.second->data, e.second->filesize) {
                if (strncmp(line, "#STOCKSHA1=", 11) == 0) {
                    strncpy(sha1, line + 12, 40);
                    sha1[40] = '\0';
                    return strdup(sha1);
                }
            }
        } else if (e.first == ".backup/.myfuck") {
            for_each_line(line, e.second->data, e.second->filesize) {
                if (strncmp(line, "SHA1=", 5) == 0) {
                    strncpy(sha1, line + 5, 40);
                    sha1[40] = '\0';
                    return strdup(sha1);
                }
            }
        } else if (e.first == ".backup/.sha1") {
            return (char *) e.second->data;
        }
    }
    return nullptr;
}

#define for_each_str(str, buf, size) \
for (str = (char *) buf; str < (char *) buf + size; str = str += strlen(str) + 1)

void myfuck_cpio::restore() {
    decompress();

    if (auto it = entries.find(".backup/.rmlist"); it != entries.end()) {
        char *file;
        for_each_str(file, it->second->data, it->second->filesize)
            rm(file, false);
        rm(it);
    }

    for (auto it = entries.begin(); it != entries.end();) {
        auto cur = it++;
        if (str_starts(cur->first, ".backup")) {
            if (cur->first.length() == 7 || cur->first.substr(8) == ".myfuck") {
                rm(cur);
            } else {
                mv(cur, &cur->first[8]);
            }
        } else if (str_starts(cur->first, "myfuck") ||
                cur->first == "overlay/init.myfuck.rc" ||
                cur->first == "sbin/magic_mask.sh" ||
                cur->first == "init.myfuck.rc") {
            // Some known stuff we can remove
            rm(cur);
        }
    }
}

void myfuck_cpio::backup(const char *orig) {
    if (access(orig, R_OK))
        return;
    entry_map bkup_entries;
    string remv;

    auto b = new cpio_entry(".backup", S_IFDIR);
    bkup_entries[b->filename].reset(b);

    myfuck_cpio o(orig);

    // Remove possible backups in original ramdisk
    o.rm(".backup", true);
    rm(".backup", true);

    auto lhs = o.entries.begin();
    auto rhs = entries.begin();

    while (lhs != o.entries.end() || rhs != entries.end()) {
        int res;
        bool backup = false;
        if (lhs != o.entries.end() && rhs != entries.end()) {
            res = lhs->first.compare(rhs->first);
        } else if (lhs == o.entries.end()) {
            res = 1;
        } else {
            res = -1;
        }

        if (res < 0) {
            // Something is missing in new ramdisk, backup!
            backup = true;
            fprintf(stderr, "Backup missing entry: ");
        } else if (res == 0) {
            if (lhs->second->filesize != rhs->second->filesize ||
                memcmp(lhs->second->data, rhs->second->data, lhs->second->filesize) != 0) {
                // Not the same!
                backup = true;
                fprintf(stderr, "Backup mismatch entry: ");
            }
        } else {
            // Something new in ramdisk
            remv += rhs->first;
            remv += (char) '\0';
            fprintf(stderr, "Record new entry: [%s] -> [.backup/.rmlist]\n", rhs->first.data());
        }
        if (backup) {
            string back_name(".backup/");
            back_name += lhs->first;
            fprintf(stderr, "[%s] -> [%s]\n", lhs->first.data(), back_name.data());
            auto ex = static_cast<cpio_entry*>(lhs->second.release());
            ex->filename = back_name;
            bkup_entries[ex->filename].reset(ex);
        }

        // Increment positions
        if (res < 0) {
            ++lhs;
        } else if (res == 0) {
            ++lhs; ++rhs;
        } else {
            ++rhs;
        }
    }

    if (!remv.empty()) {
        auto rmlist = new cpio_entry(".backup/.rmlist", S_IFREG);
        rmlist->filesize = remv.length();
        rmlist->data = xmalloc(remv.length());
        memcpy(rmlist->data, remv.data(), remv.length());
        bkup_entries[rmlist->filename].reset(rmlist);
    }

    if (bkup_entries.size() > 1)
        entries.merge(bkup_entries);
}

void myfuck_cpio::compress() {
    if (exists(RAMDISK_XZ))
        return;
    fprintf(stderr, "Compressing cpio -> [%s]\n", RAMDISK_XZ);
    auto init = entries.extract("init");

    uint8_t *data;
    size_t len;
    auto strm = make_stream_fp(get_encoder(XZ, make_unique<byte_stream>(data, len)));
    dump(strm.release());

    entries.clear();
    entries.insert(std::move(init));
    auto xz = new cpio_entry(RAMDISK_XZ, S_IFREG);
    xz->data = data;
    xz->filesize = len;
    insert(xz);
}

void myfuck_cpio::decompress() {
    auto it = entries.find(RAMDISK_XZ);
    if (it == entries.end())
        return;
    fprintf(stderr, "Decompressing cpio [%s]\n", RAMDISK_XZ);

    char *data;
    size_t len;
    {
        auto strm = get_decoder(XZ, make_unique<byte_stream>(data, len));
        strm->write(it->second->data, it->second->filesize);
    }

    entries.erase(it);
    load_cpio(data, len);
    free(data);
}

int cpio_commands(int argc, char *argv[]) {
    char *incpio = argv[0];
    ++argv;
    --argc;

    myfuck_cpio cpio;
    if (access(incpio, R_OK) == 0)
        cpio.load_cpio(incpio);

    int cmdc;
    char *cmdv[6];

    for (int i = 0; i < argc; ++i) {
        // Reset
        cmdc = 0;
        memset(cmdv, 0, sizeof(cmdv));

        // Split the commands
        char *tok = strtok(argv[i], " ");
        while (tok && cmdc < std::size(cmdv)) {
            if (cmdc == 0 && tok[0] == '#')
                break;
            cmdv[cmdc++] = tok;
            tok = strtok(nullptr, " ");
        }

        if (cmdc == 0)
            continue;

        if (cmdv[0] == "test"sv) {
            exit(cpio.test());
        } else if (cmdv[0] == "restore"sv) {
            cpio.restore();
        } else if (cmdv[0] == "sha1"sv) {
            char *sha1 = cpio.sha1();
            if (sha1) printf("%s\n", sha1);
            return 0;
        } else if (cmdv[0] == "compress"sv){
            cpio.compress();
        } else if (cmdv[0] == "decompress"sv){
            cpio.decompress();
        } else if (cmdv[0] == "patch"sv) {
            cpio.patch();
        } else if (cmdc == 2 && cmdv[0] == "exists"sv) {
            exit(!cpio.exists(cmdv[1]));
        } else if (cmdc == 2 && cmdv[0] == "backup"sv) {
            cpio.backup(cmdv[1]);
        } else if (cmdc >= 2 && cmdv[0] == "rm"sv) {
            bool r = cmdc > 2 && cmdv[1] == "-r"sv;
            cpio.rm(cmdv[1 + r], r);
        } else if (cmdc == 3 && cmdv[0] == "mv"sv) {
            cpio.mv(cmdv[1], cmdv[2]);
        } else if (cmdv[0] == "extract"sv) {
            if (cmdc == 3) {
                return !cpio.extract(cmdv[1], cmdv[2]);
            } else {
                cpio.extract();
                return 0;
            }
        } else if (cmdc == 3 && cmdv[0] == "mkdir"sv) {
            cpio.mkdir(strtoul(cmdv[1], nullptr, 8), cmdv[2]);
        } else if (cmdc == 3 && cmdv[0] == "ln"sv) {
            cpio.ln(cmdv[1], cmdv[2]);
        } else if (cmdc == 4 && cmdv[0] == "add"sv) {
            cpio.add(strtoul(cmdv[1], nullptr, 8), cmdv[2], cmdv[3]);
        } else {
            return 1;
        }
    }

    cpio.dump(incpio);
    return 0;
}