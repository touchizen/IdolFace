package com.touchizen.idolface.utils

sealed class DetectState {

    class OnSuccess(val data: Any?=null): DetectState(){
        override fun toString(): String {
            return "OnSuccess State"
        }
    }

    class OnFailure(val e: Exception): DetectState(){
        override fun toString(): String {
            return "OnFailure State"
        }
    }

    object OnDetecting : DetectState() {
        override fun toString(): String {
            return "OnDetecting State"
        }
    }

    object OnIdolDetecting : DetectState() {
        override fun toString(): String {
            return "OnIdolDetecting State"
        }
    }

    object OnRecognizing : DetectState() {
        override fun toString(): String {
            return "OnRecognizing State"
        }
    }

    object OnReady : DetectState() {
        override fun toString(): String {
            return "OnReady State"
        }
    }

}