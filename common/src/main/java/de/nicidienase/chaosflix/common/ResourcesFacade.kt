package de.nicidienase.chaosflix.common

import android.content.Context
import android.content.res.Resources
import android.support.annotation.StringRes

class ResourcesFacade(context: Context) {
	private val resources: Resources = context.applicationContext.resources

	fun getString(@StringRes id: Int, vararg any: Any): String = resources.getString(id,any)
}