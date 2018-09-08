package de.nicidienase.chaosflix.common.util

class LiveEvent<T,U,V>(
		val state: T,
		val data: U? = null,
		val error: V? = null)