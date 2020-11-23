package com.example.kakaotalknotification.Game

import android.util.Log
import java.lang.NumberFormatException
import java.util.*

class NumberBaseballGame : Game() {

    // 숫자만들기
    fun makeNumber() {
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

        this.botAnswer = resultNumber
        this.gameMessage += "******** 진행상황 *********\n"
        this.gameMessage += "입력숫자 | 스트라이크 | 볼\n"

        Log.e("Listener", "숫자야구 정답 : " + this.botAnswer)
    }

    // 입력한 숫자 유효성 검사
    fun examineNumber(number: String) : Boolean {
        var result = false

        // 숫자만 입력되었는지 확인
        // 글자수 유효검사
        if (isInt(number) && number.length == 3) {
            // 중복성 유효검사
            var count = 0
            for (i in 0..number.length-1) {
                for (j in i+1..2) {
                    if (number[i] == number[j]) {
                        count++
                    }
                }
            }

            if (count == 0) {
                result = true
            }
        }

        return result
    }

    fun checkNumber(nickName: String, clientAnswer: String) : Boolean {
        this.nickName = nickName
        this.clientAnswer = clientAnswer
        var result = false

        var strike = 0
        var ball = 0

        for (i in 0..2) {
            for (j in 0..2) {
                if (this.botAnswer[i] == clientAnswer[j]) {
                    if (i == j) {
                        strike++
                    } else {
                        ball++
                    }
                }
            }
        }

        if (strike == 3) {
            result = true
            this.winnerMessage += "[둥봇메세지]\n"
            this.winnerMessage += nickName + " 님 정답입니다!!"
        } else {
            this.gameMessage += "   " + clientAnswer + "    |        " + strike + "       |   " + ball + "\n"
        }

        return result
    }

    // 게임 끝난 후 초기화 메서드
    fun gameClear() {
        this.kakaoRoom = ""
        this.nickName = ""
        this.clientAnswer = ""
        this.botAnswer = ""
        this.gameMessage = ""
        this.winnerMessage = ""
    }

    // 숫자인지 판별 메서드
    fun isInt(number: String) : Boolean{
        try {
            Integer.parseInt(number)
            return true
        } catch (e: NumberFormatException) {
            return false
        }
    }
}