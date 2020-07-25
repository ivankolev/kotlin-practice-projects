package coffee_machine

import com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOutNormalized
import machine.CoffeeMachine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


internal class Coffee_machineKtTest {
    lateinit var coffeeMachine: CoffeeMachine

    @BeforeEach
    fun setUpMachine() {
        coffeeMachine = CoffeeMachine(400, 540, 120, 9, 550)
    }

    @Test
    fun executeCommandRemaining() {
        val outPut = tapSystemOutNormalized {
            coffeeMachine.executeCommand("remaining")
        }
        val expected = """
            
            The coffee machine has:
            400 of water
            540 of milk
            120 of coffee beans
            9 of disposable cups
            $550 of money
            """.trimIndent()

        assertEquals(expected, outPut.trimEnd())
    }

    @Test
    fun executeCommandBuy() {
        val outPut = tapSystemOutNormalized {
            coffeeMachine.executeCommand("buy")
        }
        val expected = """
            What do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino, back - to main menu: > 
            """.trimIndent()
        assertEquals(expected, outPut.trimIndent())

        val buyOutput = tapSystemOutNormalized {
            coffeeMachine.executeCommand("1")
        }
        val expectedWorking = """
            I have enough resources, making you a coffee!
            
        """.trimIndent()
        assertEquals(expectedWorking, buyOutput.trimIndent())

        val outPut2 = tapSystemOutNormalized {
            coffeeMachine.executeCommand("buy")
        }
        val expected2 = """
            What do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino, back - to main menu: > 
            """.trimIndent()
        assertEquals(expected2, outPut2.trimIndent())

        val buyOutput2 = tapSystemOutNormalized {
            coffeeMachine.executeCommand("2")
        }
        val expectedNotWorking = """
            Sorry, not enough water!
            
        """.trimIndent()
        assertEquals(expectedNotWorking, buyOutput2.trimIndent())
    }

    @Test
    fun executeCommandTake() {
        val outPut = tapSystemOutNormalized {
            coffeeMachine.executeCommand("take")
        }
        val expected = """
            
            I gave you $550
            
            
            """.trimIndent()
        assertEquals(expected, outPut)
    }
}
