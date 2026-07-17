package io.github.droidkaigi.confsched.core.common

/**
 * Marks a type parameter whose type argument must be a `@Serializable` class. Reified
 * `serializer<T>()` lookups fail only at runtime; the MustBeSerializable FIR checker turns a
 * non-serializable argument at any call site into a compile error.
 */
@Target(AnnotationTarget.TYPE_PARAMETER)
annotation class MustBeSerializable
