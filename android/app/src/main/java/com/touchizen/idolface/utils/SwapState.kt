package com.touchizen.idolface.utils

sealed class SwapState {

    class OnSuccess(val data: Any?=null): SwapState(){
        override fun toString(): String {
            return "OnSuccess State"
        }
    }

    class OnFailure(val e: Exception): SwapState(){
        override fun toString(): String {
            return "OnFailure State"
        }
    }

    object OnSwaping : SwapState() {
        override fun toString(): String {
            return "OnSwaping State"
        }
    }
}