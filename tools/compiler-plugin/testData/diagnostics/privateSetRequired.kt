class Counter {
    private var mutableCount = 0
    val <!PROPERTY_MUST_USE_PRIVATE_SET!>count<!>: Int get() = mutableCount
}

class CounterWithPrivateSet {
    var count: Int = 0
        private set
}
