package org.matamercer.domain.services.storage.exceptions

open class StorageTransactionException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
