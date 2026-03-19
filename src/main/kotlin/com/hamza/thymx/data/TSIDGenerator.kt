package com.hamza.thymx.data

import io.hypersistence.tsid.TSID
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

// https://vladmihalcea.com/uuid-database-primary-key
// https://github.com/vladmihalcea/hypersistence-tsid
object TSIDGenerator {
    fun next(): TSID = TSID.Factory.getTsid()
}

@Converter(autoApply = true)
class TSIDAttributeConverter : AttributeConverter<TSID, Long> {
    override fun convertToDatabaseColumn(attribute: TSID?): Long? = attribute?.toLong()

    override fun convertToEntityAttribute(dbData: Long?): TSID? = dbData?.let { TSID.from(it) }
}

fun TSID.encodeToString(): String = this.encode(62)

fun String.decodeToTSID(): TSID = TSID.decode(this, 62)
