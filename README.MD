![](docs/images/logo.png)

[![Downloads](https://img.shields.io/badge/dynamic/json?color=green&label=Downloads&query=totalString&url=https%3A%2F%2Fraw.githubusercontent.com%2Ftopjohnwu%2Fmyfuck-files%2Fcount%2Fcount.json&cacheSeconds=1800)](https://raw.githubusercontent.com/topjohnwu/myfuck-files/count/count.json)

#### This is not an officially supported Google product

## Introduction

Myfuck is a suite of open source software for customizing Android, supporting devices higher than Android 5.0.<br>
Some highlight features:

- **MyfuckSU**: Provide root access for applications
- **Myfuck Modules**: Modify read-only partitions by installing modules
- **MyfuckBoot**: The most complete tool for unpacking and repacking Android boot images
- **Zygisk**: Run code in every Android applications' processes

## Downloads

[Github](https://github.com/topjohnwu/Myfuck/) is the only source where you can get official Myfuck information and downloads.

[![](https://img.shields.io/badge/Myfuck-v25.1-blue)](https://github.com/topjohnwu/Myfuck/releases/tag/v25.1)
[![](https://img.shields.io/badge/Myfuck%20Beta-v25.1-blue)](https://github.com/topjohnwu/Myfuck/releases/tag/v25.1)
[![](https://img.shields.io/badge/Myfuck-Canary-red)](https://raw.githubusercontent.com/topjohnwu/myfuck-files/canary/app-release.apk)
[![](https://img.shields.io/badge/Myfuck-Debug-red)](https://raw.githubusercontent.com/topjohnwu/myfuck-files/canary/app-debug.apk)

## Useful Links

- [Installation Instruction](https://topjohnwu.github.io/Myfuck/install.html)
- [Myfuck Documentation](https://topjohnwu.github.io/Myfuck/)
- [Myfuck Troubleshoot Wiki](https://www.didgeridoohan.com/myfuck/HomePage) (by [@Didgeridoohan](https://github.com/Didgeridoohan))

## Bug Reports

**Only bug reports from Debug builds will be accepted.**

For installation issues, upload both boot image and install logs.<br>
For Myfuck issues, upload boot logcat or dmesg.<br>
For Myfuck app crashes, record and upload the logcat when the crash occurs.

## Building and Development

- Myfuck builds on any OS Android Studio supports. Install Android Studio and do the initial setups.
- Clone sources: `git clone --recurse-submodules https://github.com/topjohnwu/Myfuck.git`
- Install Python 3.6+ \
  (Windows only: select **'Add Python to PATH'** in installer, and run `pip install colorama` after install)
- Configure to use the JDK bundled in Android Studio:
  - macOS: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jre/Contents/Home"`
  - Linux: `export PATH="/path/to/androidstudio/jre/bin:$PATH"`
  - Windows: Add `C:\Path\To\Android Studio\jre\bin` to environment variable `PATH`
- Set environment variable `ANDROID_SDK_ROOT` to the Android SDK folder (can be found in Android Studio settings)
- Run `./build.py ndk` to let the script download and install NDK for you
- To start building, run `build.py` to see your options. \
  For each action, use `-h` to access help (e.g. `./build.py all -h`)
- To start development, open the project with Android Studio. The IDE can be used for both app (Kotlin/Java) and native sources.
- Optionally, set custom configs with `config.prop`. A sample `config.prop.sample` is provided.

## Signing and Distribution

- The certificate of the key used to sign the final Myfuck APK product is also directly embedded into some executables. In release builds, Myfuck's root daemon will enforce this certificate check and reject and forcefully uninstall any non-matching Myfuck apps to protect users from malicious and unverified Myfuck APKs.
- To do any development on Myfuck itself, switch to an **official debug build and reinstall Myfuck** to bypass the signature check.
- To distribute your own Myfuck builds signed with your own keys, set your signing configs in `config.prop`.
- Check [Google's Documentation](https://developer.android.com/studio/publish/app-signing.html#generate-key) for more details on generating your own key.

## Translation Contributions

Default string resources for the Myfuck app and its stub APK are located here:

- `app/src/main/res/values/strings.xml`
- `stub/src/main/res/values/strings.xml`

Translate each and place them in the respective locations (`[module]/src/main/res/values-[lang]/strings.xml`).

## License

    Myfuck, including all git submodules are free software:
    you can redistribute it and/or modify it under the terms of the
    GNU General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
