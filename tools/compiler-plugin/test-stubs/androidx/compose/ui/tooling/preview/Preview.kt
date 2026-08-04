package androidx.compose.ui.tooling.preview

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Preview

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class PreviewWrapper(val wrapper: KClass<*>)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class PreviewParameter(val provider: KClass<out PreviewParameterProvider<*>>)

interface PreviewParameterProvider<T> {
    val values: Sequence<T>
}
