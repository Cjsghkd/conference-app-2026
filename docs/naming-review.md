# Naming review

A name is read far more often than the declaration it labels, and it is the only part of a declaration that reaches a call site. This page states when a name and its type disagree, and how a reviewer applies the rule. It covers value declarations — properties, parameters, and return values; Compose view naming is defined in [Building a screen](./building-a-screen.md#naming-conventions-for-compose-views).

## Name and type

The name states **what the value is**; the type states **how it is represented**. A name that denotes an entity on a general-purpose type breaks that split, because the value is not the entity — it is one attribute of it.

```kotlin
data class TimetableItem(
    val speaker: String,   // rejected: the value is not a speaker, it is a speaker's name
    …
)
```

A property named `speaker` promises a speaker, so one call site writes `item.speaker.name`; the type promises text, so another writes `Text(item.speaker)`. Nothing in the declaration says which reading holds. The name must denote the attribute it carries:

```kotlin
val speakerName: String
```

The check is one question: **is the value of this declaration a `<name>`?** For `speaker: String` the answer is no, and the correction is the `<entity><Attribute>` form — `speakerName`, `roomName`, `sponsorLogoUrl`. The bare entity name belongs to the declaration whose type models the entity: `val speaker: Speaker`.

## Consequences

- **Call sites are written against the promise.** A name that reads as an entity invites `speaker.name` and `speaker.iconUrl` — code that has to be un-written once the type is read.
- **A wrong operation looks right.** `sessions.groupBy { it.speaker }` reads as grouping by speaker and in fact groups by display text, merging two people who share a name. `groupBy { it.speakerName }` puts the defect in view at the line that has it.
- **The entity name is on loan.** `speaker: String` also decides, silently, that a session has one speaker identified by text. When an icon or a second speaker arrives the property is renamed anyway, whereas `speakerName` leaves `speaker` free for the type that will own it.

## Choosing the correction

Two corrections satisfy the rule; the difference is whether the entity already exists in the domain.

| Situation | Correction |
| --- | --- |
| The value is that one attribute — the app displays it and reads nothing else | Rename to `<entity><Attribute>` |
| Call sites already need two attributes together, or compare identity | Introduce the type, keep the bare name |

## Over-qualification

The suffix names the attribute, not the type. `title: String` is already an attribute name and stays as it is; `titleString` and `titleText` restate what the type declares. A suffix is added only where the name currently denotes an entity.

## Related mismatches

The same disagreement appears wherever the type carries less structure than the name promises.

| Rejected | Read as | Required |
| --- | --- | --- |
| `val speaker: SpeakerId` | a speaker | `speakerId` — a property holding an identity says so, even though the value class also does |
| `val favorite: Boolean` | a favorite | `isFavorite` — the value answers an assertion, so a noun takes `is` / `has` / `can` |
| `val speaker: List<String>` | one speaker | `speakerNames` — a collection is plural, and the element attribute is named |

An adjective or a participle already reads as an assertion and stands on its own — `enabled`, `dataCleared`. Adding a prefix there is over-qualification, and it contradicts the Compose parameter it feeds (`Button(enabled = …)`). The prefix is required where the bare word also names a thing in this domain: a favorite is an element of `Timetable.bookmarks`, so `favorite` alone reads as one of them.

## Review procedure

For each declaration in the diff whose type is general-purpose (`String`, `Int`, `Boolean`, or a collection of those):

1. Read the name on its own and state the value it promises.
2. Compare that promise against the type. Agreement ends the check.
3. On a mismatch, pick a correction from [Choosing the correction](#choosing-the-correction) and report it together with the call site that reads worst under the current name.

Domain models in `:core:model` and `UiState` properties come first: their names reach every feature that renders them.

## Scope of static enforcement

Separating an entity word from an attribute word requires the domain vocabulary — `title` and `speaker` are both nouns, and only the conference domain tells them apart. A FIR checker would need that vocabulary as a hard-coded list, and curating the list is the review it would replace. The rule therefore stays at level 3 of the [Enforcement](./enforcement.md) hierarchy.

Related: [Enforcement](./enforcement.md) · [Building a screen](./building-a-screen.md)
