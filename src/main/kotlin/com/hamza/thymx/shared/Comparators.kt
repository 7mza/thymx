package com.hamza.thymx.shared

import org.thymeleaf.spring6.util.DetailedError

class DetailedErrorComparator : Comparator<DetailedError> {
    override fun compare(
        o1: DetailedError,
        o2: DetailedError,
    ): Int = o1.fieldName.compareTo(o2.fieldName)
}

internal class Toto
