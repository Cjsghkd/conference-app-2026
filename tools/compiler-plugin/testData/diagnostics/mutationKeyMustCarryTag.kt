import io.github.droidkaigi.confsched.core.model.MutationTag
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.buildMutationKey

<!MUTATION_KEY_WITHOUT_TAG!>class BookmarkMutationKey(
    private val itemId: String,
) : MutationKey<Unit, String> by buildMutationKey(
    id = MutationId("bookmark/$itemId"),
    mutate = { },
)<!>

class TaggedMutationKey(
    private val tag: MutationTag,
) : MutationKey<Unit, String> by buildMutationKey(
    id = MutationId("bookmark/${tag.value}"),
    mutate = { },
)

class UntaggedIdMutationKey(
    <!MUTATION_TAG_NOT_IN_ID!>private val tag: MutationTag<!>,
) : MutationKey<Unit, String> by buildMutationKey(
    id = MutationId("bookmark"),
    mutate = { },
)
