pub use base;
pub use logging::*;

mod logging;

#[cxx::bridge]
pub mod ffi {
    extern "Rust" {
        fn rust_test_entry();
        fn android_logging();
        fn myfuck_logging();
        fn zygisk_logging();
    }
}

fn rust_test_entry() {}
