package machine

import java.util.*
import kotlin.system.exitProcess

interface CoffeeFlavour {
    val price: Int
    val coffeeBeansDose: Int
    val milkDose: Int
    val waterDose: Int

    fun enoughSupplies(machine: CoffeeMachine): Boolean {
        val waterAvailable = if (this.waterDose == 0) machine.water else machine.water / this.waterDose
        if (waterAvailable < 1) machine.shortOnWater = true
        val milkAvailable = if (this.milkDose == 0) machine.milk else machine.milk / this.milkDose
        if (milkAvailable < 1) machine.shortOnMilk = true
        val beansAvailable = if (this.coffeeBeansDose == 0) machine.coffeeBeans else machine.coffeeBeans / this.coffeeBeansDose
        if (beansAvailable < 1) machine.shortOnCoffeeBeans = true
        val cupsAvailable = machine.cups - 1
        if (cupsAvailable < 1) machine.shortOnCups = true
        return listOf(waterAvailable, beansAvailable, milkAvailable, machine.cups).min()!! > 0
    }

    fun dispense(machine: CoffeeMachine) {
        println("I have enough resources, making you a coffee!")
        machine.water -= this.waterDose
        machine.milk -= this.milkDose
        machine.coffeeBeans -= this.coffeeBeansDose
        machine.cups--
        machine.money += this.price
        println()
    }

    fun maybeServe(machine: CoffeeMachine) {
        if (this.enoughSupplies(machine)) {
            this.dispense(machine)
        } else {
            this.notEnoughSupplies(machine)
        }
        machine.currentState = CoffeeMachineState.START
    }

    fun notEnoughSupplies(machine: CoffeeMachine) {
        val supply = when {
            machine.shortOnWater -> "water"
            machine.shortOnMilk -> "milk"
            machine.shortOnCoffeeBeans -> "beans"
            machine.shortOnCups -> "cups"
            else -> "...actually all seems good"
        }
        println("Sorry, not enough $supply!")
        println()
    }
}

enum class CoffeeMachineState(val state: String) {
    START("start"),
    BUY("buy"),
    FILL("fill"),
    TAKE("take"),
    REMAINING("remaining"),
    EXIT("exit");

    companion object {
        fun isValidState(state: String): Boolean {
            for (enum in values()) {
                if (state == enum.state) {
                    return true
                }
            }
            return false
        }
    }
}

enum class CoffeeMachineFillStep(val step: String) {
    FILL_WATER("fill_water"),
    FILL_MILK("fill_milk"),
    FILL_COFFEE_BEANS("fill_coffee_beans"),
    FILL_CUPS("fill_cups"),
    NULL("")
}

enum class CoffeeMachineBuyStep(val step: String) {
    ESPRESSO("1"),
    LATTE("2"),
    CAPPUCCINO("3"),
    BACK("back")
}

data class CoffeeMachine(var water: Int, var milk: Int, var coffeeBeans: Int, var cups: Int, var money: Int) {
    var shortOnWater = false
    var shortOnMilk = false
    var shortOnCoffeeBeans = false
    var shortOnCups = false
    var currentState: CoffeeMachineState = CoffeeMachineState.START
    var currentFillStep: CoffeeMachineFillStep = CoffeeMachineFillStep.NULL

    inner class Espresso : CoffeeFlavour {
        override val waterDose = 250
        override val milkDose = 0
        override val coffeeBeansDose = 16
        override val price = 4
    }

    inner class Latte : CoffeeFlavour {
        override val waterDose = 350
        override val milkDose = 75
        override val coffeeBeansDose = 20
        override val price = 7
    }

    inner class Cappuccino : CoffeeFlavour {
        override val waterDose = 200
        override val milkDose = 100
        override val coffeeBeansDose = 12
        override val price = 6
    }

    private fun printState() {
        println()
        println("The coffee machine has:")
        println("$water of water")
        println("$milk of milk")
        println("$coffeeBeans of coffee beans")
        println("$cups of disposable cups")
        println("\$$money of money")
        println()
        currentState = CoffeeMachineState.START
    }

    private fun takeMoney() {
        println()
        println("I gave you \$$money")
        println()
        this.money = 0
        currentState = CoffeeMachineState.START
    }

    private fun buyPrompt() {
        println()
        print("What do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino, back - to main menu: > ")
    }

    private fun executeBuy(command: String) {
        when (command) {
            CoffeeMachineBuyStep.ESPRESSO.step -> Espresso().maybeServe(this)
            CoffeeMachineBuyStep.LATTE.step -> Latte().maybeServe(this)
            CoffeeMachineBuyStep.CAPPUCCINO.step -> Cappuccino().maybeServe(this)
            CoffeeMachineBuyStep.BACK.step -> currentState = CoffeeMachineState.START
        }
        currentState == CoffeeMachineState.START
    }

    private fun fillPrompt() {
        println()
        print("Write how many ml of water do you want to add: > ")
        currentFillStep = CoffeeMachineFillStep.FILL_WATER
    }

    private fun executeFill(command: String) {
        when (currentFillStep) {
            CoffeeMachineFillStep.FILL_WATER -> {
                this.water += command.toInt()
                currentFillStep = CoffeeMachineFillStep.FILL_MILK
                print("Write how many ml of milk do you want to add: > ")
            }
            CoffeeMachineFillStep.FILL_MILK -> {
                this.milk += command.toInt()
                currentFillStep = CoffeeMachineFillStep.FILL_COFFEE_BEANS
                print("Write how many grams of coffee beans do you want to add: > ")
            }
            CoffeeMachineFillStep.FILL_COFFEE_BEANS -> {
                this.coffeeBeans += command.toInt()
                print("Write how many disposable cups of coffee do you want to add: > ")
                currentFillStep = CoffeeMachineFillStep.FILL_CUPS
            }
            CoffeeMachineFillStep.FILL_CUPS -> {
                this.cups += command.toInt()
                currentState = CoffeeMachineState.START
                currentFillStep = CoffeeMachineFillStep.NULL
            }
            CoffeeMachineFillStep.NULL -> return
        }
    }

    fun executeCommand(command: String) {
        if (CoffeeMachineState.isValidState(command)) {
            currentState = CoffeeMachineState.valueOf(command.toUpperCase())
            when (currentState) {
                CoffeeMachineState.BUY -> buyPrompt()
                CoffeeMachineState.FILL -> fillPrompt()
                CoffeeMachineState.TAKE -> takeMoney()
                CoffeeMachineState.REMAINING -> printState()
                CoffeeMachineState.EXIT -> exitProcess(0)
                CoffeeMachineState.START -> return
            }
        } else {
            when (currentState) {
                CoffeeMachineState.BUY -> executeBuy(command)
                CoffeeMachineState.FILL -> executeFill(command)
                else -> return
            }
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val scanner = Scanner(System.`in`)
            val coffeeMachine = CoffeeMachine(400, 540, 120, 9, 550)
            while (true) {
                if (coffeeMachine.currentState == CoffeeMachineState.START) {
                    print("Write action (buy, fill, take, remaining, exit): > ")
                }
                coffeeMachine.executeCommand(scanner.next())
            }
        }
    }
}



