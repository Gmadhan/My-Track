package com.mytrack.utils

import com.mytrack.utils.Utils.logger

object Programs {

    fun palindrome(num: Int, word: String) {
        if(!word.isNullOrEmpty()) {
            try {
                var reverse = ""
                val length = word.length - 1
                for (i in length downTo 0) {
                    reverse += word[i]
                }
                logger("Program", if (word == reverse) "$word is a palindrome. Output - $reverse" else "$word is not a palindrome. Output - $reverse")
            } catch (ex:Exception) {
                ex.printStackTrace()
            }
        }

        if (num > 0) {
            try {
                var sum = 0
                var result = num
                while (result != 0) {
                    sum = (sum * 10) + (result % 10)
                    result /= 10
                }
                logger("Program", if (result == num) "$num is Palindrome. Output - $sum" else "$num is Not Palindrome. Output - $sum")
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
    }

    fun primeNumber(num: Int) {
        var flag = false
        for (i in 2..num / 2) {
            // condition for nonprime number
            if (num % i == 0) {
                flag = true
                break
            }
        }

        /* Using while method to check*/
        /*var j = 2
        while (j <= num / 2) {
            // condition for nonprime number
            if (num % j == 0) {
                flag = true
                break
            }
            ++j
        }*/

        logger("Program",if (!flag) "$num is a prime number." else "$num is not a prime number.")
    }

    fun fibonacci(count: Int){
        var t1 = 0
        var t2 = 1
        logger("Program","Fibonacci First $count terms: ")
        /*for (i in 1..count) {
            print("$t1 , ")
            val sum = t1 + t2
            t1 = t2
            t2 = sum
        }*/

        var i = 1
        while (i <= count) {
            logger("Program","$t1 + ")

            val sum = t1 + t2
            t1 = t2
            t2 = sum

            i++
        }
    }

    fun reverseSentence() {
        var a = "welcome, how are you!"
        val arrOfStr = a.split(" ".toRegex()).toTypedArray()
        var data = ArrayList<String>()
        for (a in arrOfStr) {
            data.add(a)
        }

        var output = ""
        for(i in data.size-1 downTo 0) {
            if(data[i].contains(",")) {
                output += data[i].replace(",", "!")
            } else if(data[i].contains("!")) {
                output += data[i].replace("!", ",")
            } else {
                output += data[i] + " "
            }
        }
        println("Reverse Sentence : - $output")
    }

}