package com.example.kakaotalknotification.Game

import java.util.*

class NumberBaseballGame : Game() {

    fun makeNumber() : String {

        val random = Random()

        var resultNumber = ""
        var num = 0
        var numList = mutableListOf<Int>()

        while (true) {
            if (numList.size == 3) {
                break
            }

            num = random.nextInt(9)

            if (!numList.contains(num)) {
                numList.add(num)
            }
        }

        for (i in numList) {
            resultNumber += i.toString()
        }

        return resultNumber

    }

}