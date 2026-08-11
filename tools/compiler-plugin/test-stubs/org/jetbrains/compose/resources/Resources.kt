package org.jetbrains.compose.resources

import androidx.compose.runtime.Composable

class StringResource

class PluralStringResource

class StringArrayResource

@Composable
fun stringResource(resource: StringResource): String = ""

@Composable
fun pluralStringResource(resource: PluralStringResource, quantity: Int): String = ""

@Composable
fun stringArrayResource(resource: StringArrayResource): List<String> = emptyList()
