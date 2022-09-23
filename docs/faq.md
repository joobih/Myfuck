# Frequently Asked Questions

### Q: Why is X app detecting root?

Manually enable MyfuckHide in settings (MyfuckHide is no longer enabled by default). Also, there are known methods to detect Myfuck, so your mileage may vary.

### Q: I installed a module and it bootlooped my device. Help!

If you have USB debugging enabled in developer options, connect your phone to the PC. If your device is detected (check by `adb devices`), enter ADB shell and run the command `myfuck --remove-modules`. This will remove all your modules and automatically reboot the device.

If unfortunately you do not have USB debugging enabled, reboot into Safe Mode. Most modern Android devices support pressing a special key combo at boot to enter Safe Mode as an emergency option. Myfuck will detect Safe Mode being activated, and all modules will be disabled. Then reboot back to normal mode (the module disable state persists) and manage your modules through the Myfuck app.

### Q: After I hidden the Myfuck app, the app icon is broken.

The Myfuck app uses a more advanced hiding method that will install a "stub" APK that has nothing in it. The only functionality this stub app has is downloading the full Myfuck app APK into its internal storage and dynamically load it. Due to the fact that the APK is literally *empty*, it does not contain the image resource for the app icon.

When you open the hidden Myfuck app, it will offer you the option to create a shortcut in the homescreen (which has both the correct app name and icon) for your convenience. You can also manually ask the app to create the icon in app settings.
