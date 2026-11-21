package com.example.incidencias.util

import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import com.example.incidencias.R

// Animaciones pequeñas reutilizadas por las pantallas.

/** sacude el campo para señalar un error de validación */
fun View.sacudir() {
    startAnimation(AnimationUtils.loadAnimation(context, R.anim.sacudir))
}

/** hace visible la vista con fundido y un leve crecimiento (fotos, mensajes) */
fun View.aparecer(duracion: Long = 380) {
    alpha = 0f
    scaleX = 0.92f
    scaleY = 0.92f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(duracion)
        .setInterpolator(OvershootInterpolator(1.2f))
        .start()
}

/** pulso corto para llamar la atención sobre un cambio (p. ej. el estado) */
fun View.pulso() {
    animate().cancel()
    scaleX = 0.8f
    scaleY = 0.8f
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(450)
        .setInterpolator(OvershootInterpolator(3f))
        .start()
}
