package com.gmail.cristiandeives.bogafit

sealed class Resource<T>(
    protected open val data: T? = null,
    protected open val exception: Event<Exception>? = null
) {
    open val isFinished = true

    class Loading<T> : Resource<T>() {
        override val isFinished = false
    }

    class Success<T>(public override val data: T? = null) : Resource<T>(data)

    class Canceled<T> : Resource<T>()

    class Error<T>(ex: Exception? = null) : Resource<T>(exception = ex?.let { Event(it) }) {
        public override val exception: Event<Exception>?
            get() = super.exception
    }
}