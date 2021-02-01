package com.udacity.loadingbutton


sealed class ButtonState {
    object Test : ButtonState()
    object Idle : ButtonState()
    object Download : ButtonState()
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}