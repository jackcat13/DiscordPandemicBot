package com.corona.virus

import org.springframework.boot.runApplication


class PandemicSpringBootApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<PandemicSpringBootApplication>(*args)
        }
    }

}