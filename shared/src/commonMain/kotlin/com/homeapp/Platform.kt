package com.homeapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform