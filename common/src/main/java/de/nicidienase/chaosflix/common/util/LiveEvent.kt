package de.nicidienase.chaosflix.common.util

data class LiveEvent<T, U, V>(
    val state: T,
    val data: U? = null,
    val error: V? = null
)
