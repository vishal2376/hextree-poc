package com.vishal2376.hextreepoc

import android.content.Context
import android.os.IBinder

/**
 * Author: Vishal Singh (vishal2376)
 */
class AidlReflection(
	context: Context,
	targetPackage: String,
	interfaceClassName: String,
	binder: IBinder
) {
	private val classLoader = context.createPackageContext(
		targetPackage,
		Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
	).classLoader

	private val interfaceClass = classLoader.loadClass(interfaceClassName)
	private val stubClass = interfaceClass.declaredClasses.first { it.simpleName == "Stub" }
	private val asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", IBinder::class.java)

	// wrapped once here, reused by every call()
	private val remoteService = asInterfaceMethod.invoke(null, binder)

	fun call(
		methodName: String,
		paramTypes: Array<Class<*>> = emptyArray(),
		vararg args: Any?
	): Any? {
		val method = interfaceClass.getDeclaredMethod(methodName, *paramTypes)
		return method.invoke(remoteService, *args)
	}
}