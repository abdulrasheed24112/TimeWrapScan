package com.development.nest.time.wrap.utils

import java.io.File

open class AppEvents {
    open fun onClick(file: File) {}
    open fun onScanning() {}
    open fun onCompleted() {}
    open fun onIteMenuClick(file: File) {}
    open fun onClick() {}
}