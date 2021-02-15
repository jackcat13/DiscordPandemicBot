package com.corona.virus.game

import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class Player(@Valid @field: NotEmpty(message = "id.required") var id: String) {

    var score = 0
    var isCoronned = false

    override fun toString(): String {
        return "Nom : $id, score=$score\n"
    }


}