package com.corona.virus

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PandemicSpringBootApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<PandemicSpringBootApplication>(*args)
        }
    }

}