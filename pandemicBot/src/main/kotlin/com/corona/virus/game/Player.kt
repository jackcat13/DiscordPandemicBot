package com.corona.virus.game

import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import javax.validation.constraints.NotEmpty

@Document
data class Player(@field: NotEmpty(message = "id.required") @Id var id: String) {

    var playerName: String = ""
    var score = 0
    var isCoronned = false

    override fun toString(): String {
        return "playerName: $playerName, score=$score\n"
    }


}